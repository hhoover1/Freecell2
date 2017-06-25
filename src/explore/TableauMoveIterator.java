package explore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;

import freecellState.Move;
import freecellState.Mover;
import freecellState.Tableau;

/* 
 * The main point of this class is to generate (a portion of) the MoveTree.
 * It starts from a given start state, and generates MoveTree nodes as it
 * is iterated through the possible moves.
 */

public class TableauMoveIterator implements Iterator<MoveTree> {
	private static HashMap<String, Long> _examinedStates = new HashMap<String, Long>();
	private static long _checkedStates = 0;
	private static int _moveTreesRemoved = 0;
	private Tableau _startTableau;
	private MoveState _topMoveState;
	private MoveTree _topMoveTree;
	private int _maxDepth;
	private MoveState _current;
	private Stack<MoveState> _stateStack = new Stack<MoveState>();
	private MoveTableauPair _next = null;

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
		if (_current == null) {
			_next = null;
		} else {
			_next = getNext();
		}
	}

	public TableauMoveIterator(Tableau tab, MoveTree mt, int maxD, int curD) {
		_startTableau = tab;
		_topMoveTree = mt;
		_topMoveState = new MoveState(_startTableau, _topMoveTree, curD);
		_maxDepth = maxD;
		_current = startState();
		if (_current == null) {
			_next = null;
		} else {
			_next = getNext();
		}

		// if hasNext is false now, there's nothing to iterate
		// remove the tree because it's a dead end.
		if (!this.hasNext() && this._topMoveTree.parent() != null) {
			_moveTreesRemoved += this._topMoveTree.remove();
		}
	}

	public MoveTree descendFor(int depth, Queue<MoveTree> pmt) {
		return this.descendFor(depth, pmt, _current);
	}

	private MoveTree descendFor(int depth, Queue<MoveTree> pmt, MoveState ms) {
		MoveTree result = this._topMoveTree;
		if (depth == 0) {
			while (ms != null && ms._moves.hasNext()) {
				Move move = ms._moves.next();
				if (move != null) {
					MoveTree mt = new MoveTree(ms.tree(), move, this.fitness(_next._tableau));
					if (Mover.isWin(_next._tableau)) {
						Mover.printWin(mt);
						System.exit(1);
						// _next = getNext();
					}
				}
			}
		} else {

		}

		return result;
	}
	
	public MoveTree treeRoot() {
		return _topMoveTree;
	}

	public long checkedStates() {
		return TableauMoveIterator._checkedStates;
	}

	@Override
	public boolean hasNext() {
		return _next != null;
	}

	@Override
	public MoveTree next() {
		return nextChoose(false);
	}

	public MoveTree nextWide() {
		return nextChoose(true);
	}

	public int moveTreesRemoved() {
		return _moveTreesRemoved;
	}

	private MoveTree nextChoose(boolean wideOrDeep) {
		if (_next == null) {
			throw new NoSuchElementException();
		}

		MoveTree mt = new MoveTree(_current.tree(), _next._move, this.fitness(_next._tableau));
		if (Mover.isWin(_next._tableau)) {
			Mover.printWin(mt);
			System.exit(1);
			// _next = getNext();
		}

		if (!wideOrDeep && (_current.depth() < _maxDepth)) { // next move is
																// deep
			MoveState ms = new MoveState(_next._tableau, mt, _current.depth() + 1);
			_stateStack.push(_current);
			_current = ms;
		}

		_next = getNext();

		return mt;
	}

	private MoveTableauPair getNext() {
		if (_current == null) {
			return null;
		}

		MoveTableauPair next = null;
		while (next == null) {
			if (_current.depth() >= _maxDepth) {
				// depth limit
				_moveTreesRemoved += _current.tree().remove();
				if (!_stateStack.isEmpty()) {
					_current = _stateStack.pop();
				} else {
					break;
				}
			} else if (_current.moves().hasNext()) {
				Move nextMove = _current.moves().next();
				Tableau nt = nextTableauWith(_current._tableau, nextMove);
				if (nt != null) {
					next = new MoveTableauPair(nt, nextMove);
				}
			} else if (!_stateStack.isEmpty()) {
				_current = _stateStack.pop();
				if (!_current.moves().hasNext() && !_current.tree().hasChildren()) {
					_moveTreesRemoved += _current.tree().remove();
				}
			} else {
				break;
			}
		}

		return next;
	}

	private Tableau nextTableauWith(Tableau tableau, Move move) {
		Tableau nt = null;
		try {
			nt = Mover.move(tableau, move);
			String ntHash = nt.tableauHash();
			synchronized (_examinedStates) {
				_checkedStates += 1;
				if (_examinedStates.containsKey(ntHash)) {
					Long c = _examinedStates.get(ntHash);
					nt = null;
					_examinedStates.put(ntHash, new Long(c + 1));
				} else {
					_examinedStates.put(ntHash, new Long(1));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return nt;
	}

	private int fitness(Tableau nt) {
		return nt.fitness();
	}

	private MoveState startState() {
		if (_topMoveState != null && _topMoveState._moves.hasNext()) {
			return _topMoveState;
		}

		return null;
	}

	private class MoveTableauPair {
		Tableau _tableau;
		Move _move;

		MoveTableauPair(Tableau t, Move m) {
			_tableau = t;
			_move = m;
		}

		@Override
		public String toString() {
			return _move.toString() + _tableau.toString().substring(0, 33);
		}
	};

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
