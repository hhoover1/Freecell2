package explore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
	private Tableau _startTableau;
	private MoveState _topMoveState;
	private MoveTree _topMoveTree;
	private int _maxDepth;
	private MoveState _current;
	private Stack<MoveState> _stateStack = new Stack<MoveState>();
	private Move _next = null;

	public TableauMoveIterator(Tableau tab, int maxD) {
		_startTableau = tab;
		_topMoveTree = new MoveTree();
		_topMoveState = new MoveState(_startTableau, _topMoveTree, 0);
		_maxDepth = maxD;
		_current = startState();
		_next = getNextDeep();
	}

	public TableauMoveIterator(Tableau tab, MoveTree mt, int maxD, int curD) {
		_startTableau = tab;
		_topMoveTree = mt;
		_topMoveState = new MoveState(_startTableau, _topMoveTree, curD);
		_maxDepth = maxD;
		_current = startState();
		_next = getNextDeep();
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
		return getNextChoose(false);
	}

	public MoveTree nextWide() {
		return getNextChoose(true);
	}

	private MoveTree getNextChoose(boolean wideOrDeep) {
		if (_next == null) {
			throw new NoSuchElementException();
		}

		Tableau nt = null;
		try {
			nt = Mover.move(_current.tableau(), _next);
			String ntHash = nt.tableauHash();
			synchronized (_examinedStates) {
				_checkedStates += 1;
				if (_examinedStates.containsKey(ntHash)) {
					Long c = _examinedStates.get(ntHash);
					_examinedStates.put(ntHash, new Long(c + 1));
					_next = null;
				} else {
					_examinedStates.put(ntHash, new Long(1));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (_next == null) {
			if (wideOrDeep) {
				_next = this.getNextWide();
			} else {
				_next = this.getNextDeep();
			}
			if (this.hasNext()) {
				return this.next();
			} else {
				return null;
			}
		}

		MoveTree mt = new MoveTree(_current.tree(), _next, this.fitness(nt));
		if (Mover.isWin(nt)) {
			Mover.printWin(mt);
			if (wideOrDeep) {
				_next = this.getNextWide();
			} else {
				_next = this.getNextDeep();
			}
			if (this.hasNext()) {
				return this.next();
			} else {
				return null;
			}
		}

		MoveState ms = new MoveState(nt, mt, _current.depth() + 1);
		_stateStack.push(_current);
		_current = ms;
		if (wideOrDeep) {
			_next = this.getNextWide();
		} else {
			_next = this.getNextDeep();
		}

		return mt;
	}

	private Move getNextDeep() {
		Move next = null;
		while (next == null) {
			if (_current.depth() >= _maxDepth) {
				// depth limit
				_current.tree().remove();
				if (!_stateStack.isEmpty()) {
					_current = _stateStack.pop();
				}
			} else if (_current.moves().hasNext()) {
				next = _current.moves().next();
			} else if (!_stateStack.isEmpty()) {
				_current = _stateStack.pop();
				if (!_current.moves().hasNext() && !_current.tree().hasChildren()) {
					_current.tree().remove();
				}
			} else {
				break;
			}
		}

		return next;
	}

	private Move getNextWide() {
		Move next = null;
		if (!_stateStack.isEmpty()) {
			_current = _stateStack.pop();
		}
		
		while (next == null) {
			if (_current.moves().hasNext()) {
				return _current.moves().next();
			} else if (!_stateStack.isEmpty()) {
				_current = _stateStack.pop();
				if (!_current.moves().hasNext() && !_current.tree().hasChildren()) {
					_current.tree().remove();
				}
			} else {
				break;
			}
		}
		
		return next;
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

	private class MoveState {
		Tableau _tableau;
		ArrayIterator<Move> _moves;
		MoveTree _tree;
		int _depth;

		MoveState(Tableau t, MoveTree m, int d) {
			_tableau = t;
			_tree = m;
			_depth = d;
			Move[] mv = MoveCalculator.movesFrom(_tableau);
			_moves = new ArrayIterator<Move>(mv);
		}

		public Tableau tableau() {
			return _tableau;
		}

		public ArrayIterator<Move> moves() {
			return _moves;
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

	public Tableau tableau() {
		if (_current != null) {
			return _current.tableau();
		}

		return null;
	}
}
