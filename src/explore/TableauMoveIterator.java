package explore;

import java.util.HashMap;
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
	private static HashMap<String, Long> _examinedStates = new HashMap<String, Long>();
	private static long _checkedStates = 0;
	private static int _moveTreesRemoved = 0;
	private Tableau _startTableau;
	private MoveState _topMoveState;
	private MoveTree _topMoveTree;
	private int _maxDepth;
	private MoveState _current;

	public static void clearExamined() {
		synchronized (TableauMoveIterator._examinedStates) {
			TableauMoveIterator._examinedStates.clear();
		}
	}

	public TableauMoveIterator(Tableau tab, int maxD) {
		_startTableau = tab;
		_topMoveTree = new MoveTree();
		_topMoveState = new MoveState(_startTableau, _topMoveTree, 0);
		_maxDepth = maxD;
		_current = startState();
	}

	public TableauMoveIterator(Tableau tab, MoveTree mt, int maxD, int curD) {
		_startTableau = tab;
		_topMoveTree = mt;
		_topMoveState = new MoveState(_startTableau, _topMoveTree, curD);
		_maxDepth = maxD;
		_current = startState();
	}

	// descendFor will start from the tableau it was constructed with,
	// and iterate over all of the possible moves not beyond depth.
	// the iteration happens depth-first.
	// it returns the top of the MoveTree from where it started.
	public MoveTree descendFor(int depth, Queue<MoveTree> pmt) {
		MoveTree result = null;
		if (_current != null) {
			result = this.descendFor(depth, pmt, _current);
		}

		return result;
	}

	// So - we descend,
	// The fields _current and _next are important to the existing code
	// but we'd prefer to move to parameters...
	private MoveTree descendFor(int depth, Queue<MoveTree> pmt, MoveState moveState) {
		if (depth > 0) {
			while (moveState.moves().hasNext()) {
				MoveState newMoveState = createNextMoveState(moveState);
				if (newMoveState != null) {
					if (newMoveState.depth() < _maxDepth) {
						this.descendFor(depth - 1, pmt, newMoveState);
					} else {
						_moveTreesRemoved += newMoveState.tree().remove();
					}
				}
			}
			
			if (!moveState.tree().hasChildren()) {
				_moveTreesRemoved += moveState.tree().remove();
			}
		} else {
			queueLeaves(pmt, moveState);
		}

		return moveState.tree();
	}

	/**
	 * @param pmt
	 * @param moveState
	 */
	private void queueLeaves(Queue<MoveTree> pmt, MoveState moveState) {
		while (moveState.moves().hasNext()) {
			MoveState newMoveState = createNextMoveState(moveState);
			if (newMoveState != null) {
				pmt.add(newMoveState.tree());
			}
		}
	}

	/**
	 * @param moveState
	 * @return
	 */
	private MoveState createNextMoveState(MoveState moveState) {
		Move move = moveState.moves().next();
		Tableau newTableau = nextTableauWith(moveState._tableau, move, moveState.depth() + 1);

		if (newTableau != null) {
			MoveTree newMoveTree = new MoveTree(moveState.tree(), move, this.fitness(newTableau, moveState.depth() + 1));
			if (Mover.isWin(newTableau)) {
				Mover.printWin(newMoveTree);
				System.exit(1);
				// _next = getNext();
			}

			MoveState newMoveState = new MoveState(newTableau, newMoveTree, moveState.depth() + 1);
			return newMoveState;
		}

		return null;
	}

	public MoveTree treeRoot() {
		return _topMoveTree;
	}

	public long checkedStates() {
		return TableauMoveIterator._checkedStates;
	}

	public int moveTreesRemoved() {
		return _moveTreesRemoved;
	}

	private Tableau nextTableauWith(Tableau tableau, Move move, int depth) {
		Tableau nt = null;
		try {
			nt = Mover.move(tableau, move);
			if (this.fitness(nt, depth) == Integer.MAX_VALUE) {
				return null;
			}
			
			String ntHash = nt.tableauHash();
			synchronized (_examinedStates) {
				_checkedStates += 1;
				if (_examinedStates.containsKey(ntHash)) {
					Long c = _examinedStates.get(ntHash);
					if (depth >= c) {
						nt = null; // already seen at shallower depth
					} else {
						// already seen, but only deeper.
						_examinedStates.put(ntHash, new Long(depth));
					}
				} else {
					_examinedStates.put(ntHash, new Long(depth));
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
		int result = (DEPTH_BASE*DEPTH_BASE) - (adjustedDepth * adjustedDepth);
		// result *= Math.signum(-adjustedDepth);
		
		if (nt.cardsLeft() + depth > _maxDepth) {
			result = Integer.MAX_VALUE;
		}
		
		return result;
	}

	private MoveState startState() {
		if (_topMoveState != null && _topMoveState._moves.hasNext()) {
			return _topMoveState;
		}

		return null;
	}

	private class MoveState {
		Tableau _tableau;
		Move[] _moveArray;
		ArrayIterator<Move> _moves;
		MoveTree _tree;
		int _depth;

		MoveState(Tableau t, MoveTree m, int d) {
			_tableau = t;
			_tree = m;
			_depth = d;
			_moveArray = MoveCalculator.movesFrom(_tableau);
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
			return _depth;
		}
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
}
