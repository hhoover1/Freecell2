package control;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import control.MoveTreeStatisticsCalculator.MoveTreeStatistic;
import deck.Deck;
import explore.MoveTree;
import explore.TableauMoveIterator;
import freecellState.Mover;
import freecellState.Tableau;

public class StagedDepthFirstSolver {
	private static final int INTERMEDIATE_DEPTH = 6;
	public static final int MAX_EXPLORE_DEPTH = 150;
	private static final int MOVETREE_QUEUE_LENGTH = 1000000;
	private static final long STATUS_UPDATE_INTERVAL = 100000;
	private static final long TABLEAU_PRINT_INTERVAL = 1000000;
	private static final long LOG_INTERVAL = 10000000;
	private static final String DECK_38 = "2H,JS,KC,4C,3D,AH,QC,AS,8H,QH,6S,3C,6C,4H,4S,TS,5C,5D,7C,6H,4D,7D,KH,KD,5S,5H,3H,9D,7H,JC,KS,9C,8C,8D,JH,2D,9H,JD,QS,QD,6D,8S,2C,TH,7S,TC,AC,9S,AD,TD,2S,3S";
	// private static final String DECKSTRING =
	// "3H,KH,2D,KD,JD,4H,9D,TS,7C,KS,7D,QH,8C,6H,4C,9S,7H,6C,2C,2H,5D,3D,8S,JH,TC,AD,7S,QS,8D,9H,5C,6S,5S,AH,TH,KC,3C,4D,9C,AS,4S,QC,JC,AC,3S,TD,QD,8H,5H,6D,JS,2S";
	// private static final String DECKSTRING_24943 =
	// "JS,6C,AS,3H,2C,7D,7H,7S,6S,4H,3D,5C,KS,8S,5D,4C,5S,4D,8H,QD,TH,8D,TS,7C,TC,AD,JH,6H,4S,KC,QS,JD,3C,2S,9S,TD,QC,2H,QH,8C,AC,9D,9H,AH,KH,6D,KD,5H,9C,2D,3S,JC";
	private static Deck d = Deck.deckFrom(DECK_38);
	private static Tableau startTableau = new Tableau(d);
	private static long count = 0;
	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSS");
	private static final int PARALLEL_TOPS = 32;

	private int _stagedDepth = INTERMEDIATE_DEPTH;
	private int _maxExploreDepth = MAX_EXPLORE_DEPTH;
	private long _flushedTrees = 0;
	private Queue<MoveTree> _priorityMoveQueue = new PriorityQueue<MoveTree>(MOVETREE_QUEUE_LENGTH);
	private int _maxDepthExplored = 0;
	private static StagedDepthFirstSolver solver;
	private static final MoveTree _rootMoveTree = new MoveTree();
	private List<MoveTree> _wins = new ArrayList<MoveTree>(100);
	private PrintStream logOut;

	public static void main(String[] args) {
		String fName = "statistics.log";
		if (args.length > 0) {
			fName = args[0];
		}
		solver = new StagedDepthFirstSolver(fName);
		solver.runStagedDepthFirstSearch(INTERMEDIATE_DEPTH, MAX_EXPLORE_DEPTH);
	}

	public StagedDepthFirstSolver(String logName) {
		try {
			logOut = new PrintStream(logName);
		} catch (FileNotFoundException e) {
			System.err.println("failed to open log file: '" + logName + "'");
			e.printStackTrace();
		}
	}

	private class Meter implements TableauMoveIterator.ProgressionMeter {
		private StagedDepthFirstSolver _solver;

		public Meter(StagedDepthFirstSolver solver) {
			_solver = solver;
		}

		@Override
		public void progressOneNode(Tableau t, MoveTree newTree, TableauMoveIterator tmi) {
			_solver.printTrace(tmi, newTree, t, _priorityMoveQueue);
		}
	};

	void runStagedDepthFirstSearch(int stageDepth, int maxDepth) {
		_stagedDepth = stageDepth;
		_maxExploreDepth = maxDepth;
		MoveTree base = _rootMoveTree;

		System.out.println(startTableau);

		addTreesToQueue(startTableau, base, _priorityMoveQueue);
		ArrayList<MoveTree> parallelTops = new ArrayList<MoveTree>(PARALLEL_TOPS);
		int whileCount = 0;
		while (!_priorityMoveQueue.isEmpty()) {
			for (int ii = 0; ii < PARALLEL_TOPS && !_priorityMoveQueue.isEmpty(); ++ii) {
				MoveTree nextBase = _priorityMoveQueue.poll();
				parallelTops.add(nextBase);
			}

			for (MoveTree nextBase : parallelTops) {
				Tableau pt = nextBase.resultingTableau(startTableau);
				addTreesToQueue(pt, nextBase, _priorityMoveQueue);
				whileCount += 1;
			}
			parallelTops.clear();
		}

		if (_wins.isEmpty()) {
			System.out.println("No solution found");
		} else {
			System.out.println(String.format("Found %d wins:", _wins.size()));
		}
		System.out.println(String.format("while count %d, treeSize: %d", whileCount, base.treeSize()));
		int winCount = 0;
		for (MoveTree mt : _wins) {
			System.out.println(String.format("Win #%d - depth %d", ++winCount, mt.depth()));
			Mover.printWin(mt);
		}
	}

	/**
	 * This method descends the tree for the specified depth, then iterates the
	 * generated tree and puts all of the leaves into the queue.
	 * 
	 * @param parentTableau
	 * @param parentTree
	 * @param moveTreeQueue
	 */
	private void addTreesToQueue(Tableau parentTableau, MoveTree parentTree, Queue<MoveTree> moveTreeQueue) {
		TableauMoveIterator tmi = new TableauMoveIterator(parentTableau, parentTree, _maxExploreDepth,
				_maxDepthExplored);
		Meter meter = new Meter(this);

		tmi.descendFor(_stagedDepth, moveTreeQueue, meter);
		if (tmi.winOccurred()) {
			this.flushDeepTrees(moveTreeQueue, tmi.maxDepth());
			_wins.addAll(tmi.wins());
		}

		_maxDepthExplored = tmi.maxCurrentDepth();
		_maxExploreDepth = Math.min(_maxExploreDepth, tmi.maxDepth());
	}

	private void flushDeepTrees(Queue<MoveTree> moveTreeQueue, int newMaxDepth) {
		MoveTree[] queuedTrees = moveTreeQueue.toArray(new MoveTree[moveTreeQueue.size()]);
		moveTreeQueue.clear();
		for (MoveTree mt : queuedTrees) {
			Tableau t = mt.resultingTableau(startTableau);
			if (mt.depth() + t.cardsLeft() + t.trappedDepths() <= newMaxDepth) {
				moveTreeQueue.add(mt);
			} else {
				_flushedTrees += mt.remove();
			}
		}
	}

	/**
	 * @param tmi
	 * @param m
	 */
	private void printTrace(TableauMoveIterator tmi, MoveTree m, Tableau t, Queue<MoveTree> q) {
		count += 1;
		Date d = new Date();
		if (count % STATUS_UPDATE_INTERVAL == 0) {
			System.out.println(String.format("%s: %3d: %s", dateFormatter.format(d), tmi.maxCurrentDepth(), m));
			System.out.println(String.format("%8d- checked:%5d-qSize:%6d-treesKilled:%6d-repeats:%6d-flushed:%d", count,
					tmi.checkedStates(), q.size(), tmi.moveTreesRemoved(), tmi.repeatOffenders(), _flushedTrees));
		}

		if (count % TABLEAU_PRINT_INTERVAL == 0) {
			System.out.println(String.format("%s", t));
			System.out.flush();
		}

		if (count % LOG_INTERVAL == 0) {
			StatisticsPrinter sp = new StatisticsPrinter(_rootMoveTree, q);
			sp.printStatisticsOn(logOut);
			logOut.flush();
		}
	}

	public long count() {
		return count;
	}

	class StatisticsPrinter {
		MoveTreeStatisticsCalculator mtsc;
		MoveTree _root;
		Queue<MoveTree> _queue;

		StatisticsPrinter(MoveTree root, Queue<MoveTree> queue) {
			mtsc = new MoveTreeStatisticsCalculator(startTableau);
			_root = root;
			_queue = queue;
		}

		public void printStatisticsOn(PrintStream out) {
			mtsc.calculateStatistics(_root, _queue);
			out.println(String.format("Start dump at %s", dateFormatter.format(new Date())));
			out.println("global tree stats:");
			printStat(out, mtsc.globalTreeStats());
			out.println("\nby depth:");
			for (int ii = 0; ii < mtsc.treeDepth(); ++ii) {
				out.print(String.format("depth: %3d - ", ii));
				printStat(out, mtsc.treeStatAtDepth(ii));
			}
			out.print("\nglobal queue stats:");
			printStat(out, mtsc.globalQueueStats());
			for (int ii = 0; ii < mtsc.queueDepth(); ++ii) {
				MoveTreeStatistic mts = mtsc.queueStatAtDepth(ii);
				if (mts.validData()) {
					out.print(String.format("depth: %3d - ", ii));
					printStat(out, mts);
				}
			}
		}

		private void printStat(PrintStream out, MoveTreeStatistic stat) {
			String minMax = stat.formatMinMax("score: %6d, %6d - remaining: %2d, %2d");
			String p = String.format("count: %7d, minMax: %s, avgScore: %5.1f, avgRemain: %5.1f", stat.count(), minMax,
					stat.averageScore(), stat.averageRemainingMoves());
			out.println(p);
		}
	}
}
