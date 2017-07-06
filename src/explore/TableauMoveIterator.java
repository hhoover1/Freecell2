package explore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import freecellState.Move;
import freecellState.Mover;
import freecellState.Tableau;

/* 
 * The main point of this class is to generate (a portion of) the MoveTree.
 * It starts from a given start state, and generates MoveTree nodes as it
 * is iterated through the possible moves.
 */

public class TableauMoveIterator {
	private static final int DEPTH_BASE = 100;
	private static final int INITIAL_EXAMINEDSTATES_SIZE = 1000000000;
	private static HashMap<String, Integer> _examinedStates = new HashMap<String, Integer>(INITIAL_EXAMINEDSTATES_SIZE);
	private static long _checkedStates = 0;
	private static long _repeatOffenders = 0;
	private static long _moveTreesRemoved = 0;
	private int _maxDepth;
	private int _maxCurrentDepth;
	private MoveState _current;
	private List<MoveTree> _wins = new ArrayList<MoveTree>();

	public interface ProgressionMeter {
		void progressOneNode(Tableau t, MoveTree newTree, TableauMoveIterator tmi);
	};

	public static void clearExamined() {
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

	public Move[] moveArray() {
		return _current.moveArray();
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
	public MoveTree descendFor(int depth, Queue<MoveTree> pmt, ProgressionMeter meter) {
		MoveTree result = null;
		if (_current != null) {
			result = this.descendFor(depth, pmt, meter, _current);
		}

		return result;
	}

	// So - we descend,
	// The fields _current and _next are important to the existing code
	// but we'd prefer to move to parameters...
	private MoveTree descendFor(int depth, Queue<MoveTree> pmt, ProgressionMeter meter, MoveState moveState) {
		if (depth > 1 || !moveState.tableau().hasTrappedCard()) { // forget the
																	// interim
																	// depth
																	// check if
																	// no
																	// trapped
																	// cards.
			while (moveState.moves().hasNext()) {
				MoveState newMoveState = createNextMoveState(moveState, meter);
				if (newMoveState != null) {
					if (newMoveState.depth() < _maxDepth) {
						this._maxCurrentDepth = Math.max(newMoveState.depth(), _maxCurrentDepth);
						this.descendFor(depth - 1, pmt, meter, newMoveState);
					} else {
						_moveTreesRemoved += newMoveState.tree().remove();
					}
				}
			}

			if (!moveState.tree().hasChildren()) {
				_moveTreesRemoved += moveState.tree().remove();
			}
		} else {
			queueLeaves(pmt, moveState, meter);
		}

		return moveState.tree();
	}

	public boolean winOccurred() {
		return !_wins.isEmpty();
	}

	public Iterator<MoveTree> wins() {
		return _wins.iterator();
	}

	/**
	 * @param pmt
	 * @param moveState
	 */
	private void queueLeaves(Queue<MoveTree> pmt, MoveState moveState, ProgressionMeter meter) {
		while (moveState.moves().hasNext()) {
			MoveState newMoveState = createNextMoveState(moveState, meter);
			if (newMoveState != null) {
				this._maxCurrentDepth = Math.max(this._maxCurrentDepth, newMoveState.depth());
				pmt.add(newMoveState.tree());
			}
		}
	}

	/**
	 * @param moveState
	 * @return
	 */
	private MoveState createNextMoveState(MoveState moveState, ProgressionMeter meter) {
		Move move = moveState.moves().next();
		Tableau newTableau = nextTableauWith(moveState._tableau, move, moveState.depth() + 1);

		if (newTableau != null) {
			MoveTree newMoveTree = new MoveTree(moveState.tree(), move,
					this.fitness(newTableau, moveState.depth() + 1));
			meter.progressOneNode(newTableau, newMoveTree, this);

			if (Mover.isWin(newTableau)) {
				_wins.add(newMoveTree);
				Mover.printWin(newMoveTree);
				// System.exit(1);
				_maxDepth = moveState.depth() - 1;
				System.out.println(String.format("Win occurred at depth %d: setting max depth to %d", moveState.depth(),
						_maxDepth));
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
			nt = Mover.move(tableau, move);
			if (this.fitness(nt, depth) == Integer.MAX_VALUE) {
				return null;
			}

			if (!Mover.isWin(nt)) {
				String ntHash = nt.tableauHash();
				synchronized (_examinedStates) {
					_checkedStates += 1;
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
						_examinedStates.put(ntHash, new Integer(depth));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return nt;
	}

	private int fitness(Tableau nt, int depth) {
		int depthFit = depthFunction(nt, depth);
		if (depthFit == Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}

		return nt.fitness() - depthFit;
	}

	private int depthFunction(Tableau nt, int depth) {
		int adjustedDepth = (DEPTH_BASE - depth);
		int result = (DEPTH_BASE * DEPTH_BASE) - (adjustedDepth * adjustedDepth);
		// result *= Math.signum(-adjustedDepth);

		if (nt.cardsLeft() + depth > _maxDepth) {
			result = Integer.MAX_VALUE;
		}

		return result;
	}

	private class MoveState {
		Tableau _tableau;
		Move[] _moveArray;
		ArrayIterator<Move> _moves;
		MoveTree _tree;

		MoveState(Tableau t, MoveTree m) {
			_tableau = t;
			_tree = m;
			_moveArray = MoveCalculator.movesFrom(_tableau, !_tableau.hasTrappedCard());
			_moves = new ArrayIterator<Move>(_moveArray);
		}

		public Tableau tableau() {
			return _tableau;
		}

		public ArrayIterator<Move> moves() {
			return _moves;
		}

		public Move[] moveArray() {
			return _moveArray;
		}

		public MoveTree tree() {
			return _tree;
		}

		public int depth() {
			return _tree.depth();
		}
	}
}
