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
import java.util.Random;

import control.MoveTreeStatisticsCalculator.MoveTreeStatistic;
import deck.Deck;
import explore.MoveTree;
import explore.TableauMoveIterator;
import freecellState.Mover;
import freecellState.Tableau;
import main.Arguments;

public class StagedDepthFirstSolver {
	public static final String MAX_DEPTH_KEY = "maxDepth";
	public static final String INTERMEDIATE_DEPTH_KEY = "intermediate_depth";
	public static final String MAX_EXPLORE_DEPTH_KEY = "max_explore_depth";
	public static final String DECK_STRING_KEY = "deck_string";
	public static final String MOVETREE_QUEUE_LENGTH_KEY = "queueLength";
	public static final String STATUS_UPDATE_INTERVAL_KEY = "updateInterval";
	public static final String TABLEAU_PRINT_INTERVAL_KEY = "tableauPrintInterval";
	public static final String STATISTICS_LOG_INTERVAL_KEY = "statisticsInterval";
	public static final String RANDOM_PRIORITY_INTERVAL_KEY = "randomInterval";
	public static final String STATISTICS_LOG_NAME_KEY = "statLogName";
	public static final String DO_STATISTICS_KEY = "doStatistics";
	public static final String PARALLEL_TOPS_KEY = "parallelTops";
	public static final String FLUSH_DOT_COUNT_KEY = "flushDotCount";
	
	private static int INTERMEDIATE_DEPTH = 6;
	public static int MAX_EXPLORE_DEPTH = 135;
	private static final int MOVETREE_QUEUE_LENGTH = 1000000;
	private static final long TABLEAU_PRINT_INTERVAL = 1000000;
	private static final long LOG_INTERVAL = 10000000;
	private static final int RANDOM_PRIORITY_INTERVAL = 10000000;
	private static final long STATUS_UPDATE_INTERVAL = 100000;
	private static final int FLUSH_DOT_COUNT = 100000;
	public static final String DECKSTRING_38 = "2H,JS,KC,4C,3D,AH,QC,AS,8H,QH,6S,3C,6C,4H,4S,TS,5C,5D,7C,6H,4D,7D,KH,KD,5S,5H,3H,9D,7H,JC,KS,9C,8C,8D,JH,2D,9H,JD,QS,QD,6D,8S,2C,TH,7S,TC,AC,9S,AD,TD,2S,3S";
	public static final String DECKSTRING_635 = "9C,7H,3D,4D,5H,AS,4S,4H,AH,KD,8S,QC,5C,7S,JC,6C,KH,6D,7D,3S,8H,QS,JS,2D,9S,KS,AD,3C,AC,6H,6S,QH,TD,8C,TC,2H,2C,9H,KC,JD,QD,5D,4C,5S,2S,9D,TS,7C,JH,8D,TH,3H";
	public static final String DECKSTRING_3920 = "QS,TS,7C,5H,9S,QH,5S,6S,9C,AS,3D,4H,8S,2H,8H,KC,AH,3H,8C,7H,9D,5D,JD,TH,4C,6D,QC,6C,TC,KD,TD,4D,KS,AD,JS,2D,7D,6H,JC,AC,8D,4S,3C,5C,7S,9H,QD,KH,3S,JH,2C,2S";
	public static final String DECKSTRING_11987 = "5S,JD,8S,3S,3H,KC,2S,KD,4S,5D,6H,7D,6D,KH,2C,QD,3C,TD,7S,8C,QH,9H,4C,TH,8D,TC,4D,9S,7C,QC,8H,3D,7H,6C,9D,5C,QS,KS,AC,2D,JS,6S,4H,TS,9C,AD,AH,2H,JC,5H,AS,JH";
	public static final String DECKSTRING_24943 = "JS,6C,AS,3H,2C,7D,7H,7S,6S,4H,3D,5C,KS,8S,5D,4C,5S,4D,8H,QD,TH,8D,TS,7C,TC,AD,JH,6H,4S,KC,QS,JD,3C,2S,9S,TD,QC,2H,QH,8C,AC,9D,9H,AH,KH,6D,KD,5H,9C,2D,3S,JC";
	public static final String DECKSTRING_40041 = "JS,2H,2D,JC,JD,6D,5C,2S,QH,5H,JH,TD,3C,7c,AD,AS,tc,KD,5S,3D,8S,KC,QS,2C,3H,TH,4C,QD,KS,9D,8H,8D,4S,9C,3S,4D,9S,TS,7D,6H,6C,QC,AH,KH,AC,6S,7S,5D,9H,8C,7H,4H";
	public static final String DECKSTRING_170414 = "3S,6C,4H,9D,TC,9S,KD,TD,4D,8H,3H,JS,7C,4S,7D,KC,2C,TS,QD,QC,KS,7H,9H,QH,AH,9C,5D,5H,6H,2H,TH,4C,2D,AC,8S,JC,5S,6S,8C,KH,8D,5C,3C,6D,AD,3D,JH,7S,JD,2S,QS,AS";
	// private static final String DECKSTRING =
	// "3H,KH,2D,KD,JD,4H,9D,TS,7C,KS,7D,QH,8C,6H,4C,9S,7H,6C,2C,2H,5D,3D,8S,JH,TC,AD,7S,QS,8D,9H,5C,6S,5S,AH,TH,KC,3C,4D,9C,AS,4S,QC,JC,AC,3S,TD,QD,8H,5H,6D,JS,2S";
	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSS");
	private static final int PARALLEL_TOPS = 32;
	private static Deck deck;
	private static Tableau startTableau;
	private static long count = 0;
	
	private final Arguments arguments;
	//private int _maxExploreDepth = MAX_EXPLORE_DEPTH;
	private long _flushedTrees = 0;
	private Queue<MoveTree> _priorityMoveQueue;
	private int _maxDepthExplored = 0;
	private static final MoveTree _rootMoveTree = new MoveTree();
	private List<MoveTree> _wins = new ArrayList<MoveTree>(100);
	private PrintStream statisticsLog;

	public static void setupArguments(Arguments arguments) {
		arguments.putArg(makeDeckKey(38), DECKSTRING_38);
		arguments.putArg(makeDeckKey(635),  DECKSTRING_635);
		arguments.putArg(makeDeckKey(3920), DECKSTRING_3920);
		arguments.putArg(makeDeckKey(11987), DECKSTRING_11987);
		arguments.putArg(makeDeckKey(24943), DECKSTRING_24943);
		arguments.putArg(makeDeckKey(40041), DECKSTRING_40041);
		arguments.putArg(makeDeckKey(170414), DECKSTRING_170414);
		arguments.maxExploreDepth = MAX_EXPLORE_DEPTH;
		arguments.intermediateDepth = INTERMEDIATE_DEPTH;
		arguments.deckString = makeDeckKey(11987);
		arguments.moveTreeQueueLength = MOVETREE_QUEUE_LENGTH;
		arguments.statusUpdateInterval = STATUS_UPDATE_INTERVAL;
		arguments.tableauPrintInterval = TABLEAU_PRINT_INTERVAL;
		arguments.statisticsLogInterval = LOG_INTERVAL;
		arguments.randomSelectionInterval = RANDOM_PRIORITY_INTERVAL;
		arguments.statisticsLogName = "statistics.log";
		arguments.doStatistics = true;
		arguments.validation = true;
		arguments.parallelTops = PARALLEL_TOPS;
		arguments.flushDotInterval = FLUSH_DOT_COUNT;
	}
	
	public static String makeDeckKey(int deckNumber) {
		StringBuilder sb = new StringBuilder("DECK_STRING_KEY");
		sb.append('_');
		sb.append(deckNumber);
		return sb.toString();
	}
	
	public static void initialize(Arguments arguments) {
		String designatedDeck = arguments.deckString;
		deck = Deck.deckFrom(arguments.getString(designatedDeck));
		startTableau = new Tableau(deck);
		startTableau.setValidation(arguments.validation);
	}

	public StagedDepthFirstSolver(Arguments args) {
		arguments = args;
		String logName = arguments.statisticsLogName;
		try {
			statisticsLog = new PrintStream(logName);
		} catch (FileNotFoundException e) {
			System.err.println("failed to open log file: '" + logName + "'");
			e.printStackTrace();
		}
		
		_priorityMoveQueue = new PriorityQueue<MoveTree>(arguments.moveTreeQueueLength);	
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

	public void runStagedDepthFirstSearch() throws Exception {
		MoveTree base = _rootMoveTree;

		System.out.println(startTableau);

		int parallelTopCount = arguments.parallelTops;
		addTreesToQueue(startTableau, base, _priorityMoveQueue);
		ArrayList<MoveTree> parallelTops = new ArrayList<MoveTree>(parallelTopCount);
		int whileCount = parallelTopCount;
		long randomIntervalCount = arguments.randomSelectionInterval;
		while (!_priorityMoveQueue.isEmpty()) {
			if (whileCount % randomIntervalCount < parallelTopCount) {
				grabRandomOfQueue(parallelTops);
			} else {
				grabTopOfQueue(parallelTops);
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

	private void grabRandomOfQueue(ArrayList<MoveTree> parallelTops) {
		MoveTree[] allQueue = _priorityMoveQueue.toArray(new MoveTree[_priorityMoveQueue.size()]);
		Random r = new Random();
		int parallelTopCount = arguments.parallelTops;
		for (int ii = 0; ii < parallelTopCount; ++ii) {
			MoveTree m = allQueue[r.nextInt(allQueue.length)];
			parallelTops.add(m);
			_priorityMoveQueue.remove(m);
		}
	}

	/**
	 * @param parallelTops
	 */
	private void grabTopOfQueue(ArrayList<MoveTree> parallelTops) {
		int parallelTopCount = arguments.parallelTops;
		for (int ii = 0; ii < parallelTopCount; ++ii) {
			MoveTree nextBase = _priorityMoveQueue.poll();
			if (nextBase != null) {
				parallelTops.add(nextBase);
			}
		}
	}

	/**
	 * This method descends the tree for the specified depth, then iterates the
	 * generated tree and puts all of the leaves into the queue.
	 * 
	 * @param parentTableau
	 * @param parentTree
	 * @param moveTreeQueue
	 * @throws Exception 
	 */
	private void addTreesToQueue(Tableau parentTableau, MoveTree parentTree, Queue<MoveTree> moveTreeQueue) throws Exception {
		TableauMoveIterator tmi = new TableauMoveIterator(parentTableau, parentTree, arguments.maxExploreDepth,
				_maxDepthExplored);
		Meter meter = new Meter(this);

		tmi.descendFor(arguments.intermediateDepth, moveTreeQueue, meter);
		if (tmi.winOccurred()) {
			this.flushTooDeepTrees(moveTreeQueue, tmi.maxDepth());
			_wins.addAll(tmi.wins());
		}

		_maxDepthExplored = tmi.maxCurrentDepth();
		arguments.maxExploreDepth = Math.min(arguments.maxExploreDepth, tmi.maxDepth());
	}

	private void flushTooDeepTrees(Queue<MoveTree> moveTreeQueue, int newMaxDepth) {
		System.out.println("\nflushing deep trees");
		int cnt = 0;
		int flushDotCount = arguments.flushDotInterval;
		long startCount = _flushedTrees;
		MoveTree[] queuedTrees = moveTreeQueue.toArray(new MoveTree[moveTreeQueue.size()]);
		moveTreeQueue.clear();
		for (MoveTree mt : queuedTrees) {
			if (++cnt % flushDotCount == 0) {
				System.out.print(".");
			}
			
			if (mt.depth() + mt.cardsLeft() /*+ t.trappedDepths()*/ <= newMaxDepth) {
				moveTreeQueue.add(mt);
			} else {
				_flushedTrees += mt.remove();
			}
		}
		
		System.out.println("\ndone flushing trees, flushed " + (_flushedTrees - startCount));
	}

	/**
	 * @param tmi
	 * @param m
	 */
	private void printTrace(TableauMoveIterator tmi, MoveTree m, Tableau t, Queue<MoveTree> q) {
		count += 1;
		Date d = new Date();
		if (count % arguments.statusUpdateInterval == 0) {
			System.out.println(String.format("%s: %3d: %s", dateFormatter.format(d), tmi.maxCurrentDepth(), m));
			System.out.println(String.format("%8d- checked:%5d-qSize:%6d-treesKilled:%6d-repeats:%6d-flushed:%d", count,
					tmi.checkedStates(), q.size(), tmi.moveTreesRemoved(), tmi.repeatOffenders(), _flushedTrees));
		}

		if (count % arguments.tableauPrintInterval == 0) {
			System.out.println(String.format("%s", t));
			System.out.flush();
		}

		if (arguments.doStatistics && count % arguments.statisticsLogInterval == 0) {
			StatisticsPrinter sp = new StatisticsPrinter(_rootMoveTree, q);
			sp.printStatisticsOn(statisticsLog);
			statisticsLog.flush();
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
			mtsc = new MoveTreeStatisticsCalculator(startTableau, arguments);
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
