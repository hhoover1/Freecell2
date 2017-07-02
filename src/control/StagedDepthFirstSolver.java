package control;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import deck.Deck;
import explore.MoveTree;
import explore.TableauMoveIterator;
import freecellState.Tableau;

public class StagedDepthFirstSolver {
	private static final int INTERMEDIATE_DEPTH = 6;
	private static final int MAX_EXPLORE_DEPTH = 150;
	private static final int MOVETREE_QUEUE_LENGTH = 1000000;
	private static final long STATUS_UPDATE_INTERVAL = 100000;
	private static final long TABLEAU_PRINT_INTERVAL = 1000000;
	private static final String DECK_38 = "2H,JS,KC,4C,3D,AH,QC,AS,8H,QH,6S,3C,6C,4H,4S,TS,5C,5D,7C,6H,4D,7D,KH,KD,5S,5H,3H,9D,7H,JC,KS,9C,8C,8D,JH,2D,9H,JD,QS,QD,6D,8S,2C,TH,7S,TC,AC,9S,AD,TD,2S,3S";
	private static Deck d = Deck.deckFrom(DECK_38);
	private static Tableau startTableau = new Tableau(d);
	private static long count = 0;
	private static int maxDepthTraversed = 0;
	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSS");

	
	private int _stagedDepth = INTERMEDIATE_DEPTH;
	private int _maxExploreDepth = MAX_EXPLORE_DEPTH;
	private long _flushedTrees = 0;

	public static void main(String[] args) {
		StagedDepthFirstSolver solver = new StagedDepthFirstSolver();
		solver.runStagedDepthFirstSearch(INTERMEDIATE_DEPTH, MAX_EXPLORE_DEPTH);
	}

	void runStagedDepthFirstSearch(int stageDepth, int maxDepth) {
		_stagedDepth = stageDepth;
		_maxExploreDepth = maxDepth;
		MoveTree base = new MoveTree();
		PriorityQueue<MoveTree> pmt = new PriorityQueue<MoveTree>(MOVETREE_QUEUE_LENGTH);

		System.out.println(startTableau);

		addTreesToQueue(startTableau, base, pmt);
		int whileCount = 0;
		while (!pmt.isEmpty()) {
			MoveTree nextBase = pmt.poll();
			Tableau pt = nextBase.resultingTableau(startTableau);
			addTreesToQueue(pt, nextBase, pmt);
			whileCount += 1;
		}

		System.out.println("No solution found");
		System.out.println(String.format("while count %d, treeSize: %d", whileCount, base.treeSize()));
		Iterator<MoveTree> iter = base.iterator();
		int testCount = 0;
		while (iter.hasNext()) {
			MoveTree m = iter.next();
			System.out.println(String.format("%06d: %s", ++testCount, m.toString()));
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
				parentTree.depth());

		tmi.descendFor(_stagedDepth, moveTreeQueue);
		if (tmi.winOccurred()) {
			this.flushDeepTrees(moveTreeQueue, tmi.maxDepth());
		}
		
		Iterator<MoveTree> iter = parentTree.iterator();
		while (iter.hasNext()) {
			MoveTree nmt = iter.next();
			if (nmt.depth() > maxDepthTraversed) {
				maxDepthTraversed = nmt.depth();
			}

			printTrace(tmi, nmt, moveTreeQueue);
		}
	}

	private void flushDeepTrees(Queue<MoveTree> moveTreeQueue, int newMaxDepth) {
		MoveTree[] queuedTrees = moveTreeQueue.toArray(new MoveTree[moveTreeQueue.size()]);
		moveTreeQueue.clear();
		for (MoveTree mt : queuedTrees) {
			if (mt.depth() < newMaxDepth) {
				moveTreeQueue.add(mt);
			} else {
				_flushedTrees  += 1;
			}
		}
	}

	/**
	 * @param tmi
	 * @param m
	 */
	private void printTrace(TableauMoveIterator tmi, MoveTree m, Queue<MoveTree> q) {
		count += 1;
		Date d = new Date();
		if (count % STATUS_UPDATE_INTERVAL == 0) {
			System.out.println(String.format("%s: %3d: %s", 
					dateFormatter.format(d), maxDepthTraversed, m));
			System.out.println(String.format("%8d: checked-%5d: qSize-%6d: treesKilled-%6d: repeats-%6d: flushed: %d", count,
					tmi.checkedStates(), q.size(), tmi.moveTreesRemoved(), tmi.repeatOffenders(), _flushedTrees));
		}

		if (count % TABLEAU_PRINT_INTERVAL == 0) {
			System.out.println(String.format("%s", m.resultingTableau(startTableau)));
		}
	}

	public long count() {
		return count;
	}
}
