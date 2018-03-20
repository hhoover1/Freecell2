package explore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import freecellState.Move;
import freecellState.Mover;
import freecellState.Tableau;
import freecellState.TableauHash;

/* 
 * The main point of this class is to generate (a portion of) the MoveTree.
 * It starts from a given start state, and generates MoveTree nodes as it
 * is iterated through the possible moves.
 */

public class TableauMoveIterator {
	private static final int DEPTH_BASE = 100;
	private static final int INITIAL_EXAMINEDSTATES_SIZE = 10000000;
	private static ExaminedStatesMap _examinedStates = new ExaminedStatesMap(INITIAL_EXAMINEDSTATES_SIZE);
	private static long _checkedStates = 0;
	private static long _repeatOffenders = 0;
	private static long _moveTreesRemoved = 0;
	private int _maxDepth;
	private int _maxCurrentDepth;
	private MoveState _current;
	private final List<MoveTree> _wins = new ArrayList<MoveTree>();
	// private boolean triggeredDeepDive = false;

	public interface ProgressionMeter {
		void progressOneNode(Tableau t, MoveTree newTree, TableauMoveIterator tmi);
	};

	// testing only method
	static void clearExamined() {
		synchronized (TableauMoveIterator._examinedStates) {
			TableauMoveIterator._examinedStates.clear();
		}
	}

	public TableauMoveIterator(Tableau tab, MoveTree parentTree, int maxD, int maxCurrentDepth) {
		_maxDepth = maxD;
		_maxCurrentDepth = maxCurrentDepth;
		_current = new MoveState(tab, parentTree);
	}

	public ArrayIterator<Move> moves() {
		return _current.moves();
	}

	public Tableau tableau() {
		if (_current != null) {
			return _current.tableau();
		}

		return null;
	}

	public int maxDepth() {
		return _maxDepth;
	}

	public int maxCurrentDepth() {
		return _maxCurrentDepth;
	}

	// descendFor will start from the tableau it was constructed with,
	// and iterate over all of the possible moves not beyond depth.
	// the iteration happens depth-first.
	// it returns the top of the MoveTree from where it started.
	public MoveTree descendFor(int depth, Queue<MoveTree> pmt, ProgressionMeter meter) throws Exception {
		MoveTree result = null;
		if (_current != null) {
			result = this.descendFor(depth, pmt, meter, _current);
		}

		return result;
	}

	// So - we descend,
	// The fields _current and _next are important to the existing code
	// but we'd prefer to move to parameters...
	private MoveTree descendFor(int depth, Queue<MoveTree> pmt, ProgressionMeter meter, MoveState parentState)
			throws Exception {
		boolean deepDive = !parentState.tableau().hasTrappedCard();

		if (depth > 1 || deepDive) { // forget the interim depth check if no trapped cards.
			while (parentState.moves().hasNext()) {
				Move nextMove = parentState.moves().next();
				MoveState newMoveState = createNextMoveState(parentState, nextMove, meter);
				if (newMoveState != null) {
					if (newMoveState.depth() <= _maxDepth) {
						this._maxCurrentDepth = Math.max(newMoveState.depth(), _maxCurrentDepth);
						this.descendFor(depth - 1, pmt, meter, newMoveState);
					} else {
						_moveTreesRemoved += newMoveState.tree().remove(false);
					}
				}
			}

			if (!parentState.tree().hasChildren() && !parentState.tree().hasMoves()) {
				_moveTreesRemoved += parentState.tree().remove(false);
			}
		} else {
			queueLeaves(pmt, parentState, meter);
		}

		if (parentState.tree().depth() != -1 && parentState.tree().cardsLeft() != -1) {
			return parentState.tree();
		}

		return null;
	}

	public boolean winOccurred() {
		return !_wins.isEmpty();
	}

	public Collection<MoveTree> wins() {
		return _wins;
	}

	/**
	 * @param pmt
	 * @param moveState
	 * @throws Exception
	 */
	private void queueLeaves(Queue<MoveTree> pmt, MoveState moveState, ProgressionMeter meter) throws Exception {
		while (moveState.moves().hasNext()) {
			Move nextMove = moveState.moves().next();
			MoveState newMoveState = createNextMoveState(moveState, nextMove, meter);
			if (newMoveState != null) {
				this._maxCurrentDepth = Math.max(this._maxCurrentDepth, newMoveState.depth());
				pmt.add(newMoveState.tree());
			}
		}
	}

	/**
	 * @param parentState
	 * @param nextMove
	 *            TODO
	 * @return
	 * @throws Exception
	 */
	private MoveState createNextMoveState(MoveState parentState, Move nextMove, ProgressionMeter meter)
			throws Exception {
		int depth;
		nextMove.validate(parentState._tableau, depth = parentState.depth());
		Tableau newTableau = nextTableauWith(parentState._tableau, nextMove, depth + 1);

		if (newTableau != null) {
			MoveTree newMoveTree = new MoveTree(parentState.tree(), nextMove,
					this.fitness(newTableau, parentState.depth() + 1), newTableau.cardsLeft(), newTableau, depth + 1);
			meter.progressOneNode(newTableau, newMoveTree, this);

			if (Mover.isWin(newTableau)) {
				_wins.add(newMoveTree);
				Mover.printWin(newMoveTree);
				// System.exit(1);
				_maxDepth = Math.min(_maxDepth, newMoveTree.depth() - 1);
				System.out.println(String.format("Win occurred at depth %d: setting max depth to %d",
						newMoveTree.depth(), _maxDepth));
			}

			MoveState newMoveState = new MoveState(newTableau, newMoveTree);
			return newMoveState;
		}

		return null;
	}

	public long checkedStates() {
		return TableauMoveIterator._checkedStates;
	}

	public long moveTreesRemoved() {
		return _moveTreesRemoved;
	}

	public long repeatOffenders() {
		return _repeatOffenders;
	}

	private Tableau nextTableauWith(Tableau tableau, Move move, int depth) {
		Tableau nt = null;
		try {
			_checkedStates += 1;
			nt = Mover.move(tableau, move);
			if (this.fitness(nt, depth) == Integer.MAX_VALUE) {
				// this is a dead branch...
				return null;
			}

			if (!Mover.isWin(nt)) {
				TableauHash ntHash = nt.tableauHash();
				synchronized (_examinedStates) {
					if (_examinedStates.containsKey(ntHash)) {
						Integer c = _examinedStates.get(ntHash);
						if (depth >= c) {
							nt = null; // already seen at shallower depth
							_repeatOffenders += 1;
						} else {
							// already seen, but only deeper.
							_examinedStates.put(ntHash, new Integer(depth));
						}
					} else {
						// not seen before, we have now...
						_examinedStates.put(ntHash, new Integer(depth));
					}
				}
			} else {
				System.err.println("found a win:\n" + nt);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return nt;
	}

	private int fitness(Tableau nt, int depth) {
		if (nt.validation()) {
			int depthFit = depthFunction(nt, depth);
			if (depthFit == Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			}

			return nt.fitness() + depthFit;
		} else {
			return nt.fitness();
		}
	}

	private int depthFunction(Tableau nt, int depth) {
		int adjustedDepth = (DEPTH_BASE - depth);
		int result = (DEPTH_BASE * DEPTH_BASE) - (adjustedDepth * adjustedDepth);
		// result *= Math.signum(-adjustedDepth);

		if (nt.cardsLeft() + /* nt.trappedDepths() + */ depth > _maxDepth) {
			result = Integer.MAX_VALUE;
		}

		return result;
	}

	private class MoveState {
		final Tableau _tableau;
		final MoveTree _tree;

		MoveState(Tableau t, MoveTree m) {
			_tableau = t;
			_tree = m;
		}

		public Tableau tableau() {
			return _tableau;
		}

		public ArrayIterator<Move> moves() {
			return _tree.childMoves();
		}

		public MoveTree tree() {
			return _tree;
		}

		public int depth() {
			return _tree.depth();
		}
	}

	public int compactExaminedStates(int maxDepth) {
		return _examinedStates.compactExaminedStates(maxDepth);
	}

	public ExaminedStatesMap examinedStates() {
		return _examinedStates;
	}
}
