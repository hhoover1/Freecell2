package control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import deck.Deck;
import explore.MoveTree;
import explore.TableauMoveIterator;
import freecellState.Tableau;

public class StagedDepthFirstSolver {
	private static final int INTERMEDIATE_DEPTH = 4;
	private static final String DECK_38 = "2H,JS,KC,4C,3D,AH,QC,AS,8H,QH,6S,3C,6C,4H,4S,TS,5C,5D,7C,6H,4D,7D,KH,KD,5S,5H,3H,9D,7H,JC,KS,9C,8C,8D,JH,2D,9H,JD,QS,QD,6D,8S,2C,TH,7S,TC,AC,9S,AD,TD,2S,3S";
	// private static final int WIDTH_LIMIT = 3;
	private static Deck d = Deck.deckFrom(DECK_38);
	private static Tableau startTableau = new Tableau(d);
	private static int count = 0;
	private static int maxDepth = 0;

	public static void main(String[] args) {
		MoveTree base = new MoveTree();
		PriorityQueue<MoveTree> pmt = new PriorityQueue<MoveTree>(10000);

		System.out.println(startTableau);

		addTreesToQueue(startTableau, base, pmt);
//		int count = 0;
		while (!pmt.isEmpty()) {
			MoveTree nextBase = pmt.poll();
			Tableau pt = nextBase.resultingTableau(startTableau);
			addTreesToQueue(pt, nextBase, pmt);
//			if (++count > 100000) {
//				rebuildQueue(pmt);
//			}
		}
	}

	/**
	 * This method descends the tree for the specified depth, then iterates the
	 * generated tree and puts all of the leaves into the queue.
	 * 
	 * @param parentTableau
	 * @param parentTree
	 * @param pmt
	 */
	private static void addTreesToQueue(Tableau parentTableau, MoveTree parentTree, Queue<MoveTree> pmt) {
		TableauMoveIterator tmi = new TableauMoveIterator(parentTableau, parentTree, 150, parentTree.depth());
		descendFor(0, tmi, pmt);
		Iterator<MoveTree> iter = parentTree.iterator();
		while (iter.hasNext()) {
			MoveTree nmt = iter.next();
			pmt.add(nmt);
			printTrace(tmi, nmt, pmt);
		}
	}

	private static void descendFor(int depth, TableauMoveIterator tmi, Queue<MoveTree> pmt) {
		if (depth < INTERMEDIATE_DEPTH) {
			while (tmi.hasNext()) {
				MoveTree m = tmi.next();
				printTrace(tmi, m, pmt);
				descendFor(depth + 1, tmi, pmt);
			}
		} else if (depth == INTERMEDIATE_DEPTH) {
			//int count = 0;
			while (tmi.hasNext()/* && ++count < WIDTH_LIMIT*/) {
				MoveTree m = tmi.nextWide();
				if (m != null && m.depth() > maxDepth) {
					maxDepth = m.depth();
				}
				printTrace(tmi, m, pmt);
			}
		}
	}

	/**
	 * @param tmi
	 * @param m
	 */
	private static void printTrace(TableauMoveIterator tmi, MoveTree m, Queue<MoveTree> q) {
		count += 1;
		if (count % 1000000 == 0) {
			System.out.println(String.format("%8d: %d:%s-%5d - %6d, %6d", count, maxDepth, m, tmi.checkedStates(), q.size(), tmi.moveTreesRemoved()));
		}

		if (count % 10000000 == 0) {
			System.out.println(
					String.format("%s", m.resultingTableau(startTableau)));
		}
	}
	
//	private static void rebuildQueue(Queue<MoveTree> q) {
//		ArrayList<MoveTree> mtal = new ArrayList<MoveTree>(q.size());
//		for (MoveTree mt : q) {
//			mtal.add(mt);
//		}
//		q.clear();
//		q.addAll(mtal);
//	}
}
