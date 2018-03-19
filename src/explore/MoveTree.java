package explore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import freecellState.Move;
import freecellState.Mover;
import freecellState.Tableau;

public class MoveTree implements Comparable<MoveTree> {
	private static final int CHILD_START_COUNT = 1;
	private static final int CHILD_EXPAND_COUNT = 4;

	private MoveTree _parent;
	private final Move _move;
	private final Move[] _moves;
	private final ArrayIterator<Move> _moveIterator;
	private MoveTree[] _children = new MoveTree[CHILD_START_COUNT];
	private short _childCount = 0;
	private byte _depth;
	private byte _cardsLeft;
	private int _treeScore;

	public MoveTree(MoveTree p, Move m, int score, int left, Tableau tableau, int depth) {
		_parent = p;
		_move = m;
		_moves = MoveCalculator.movesFrom(tableau, !tableau.hasTrappedCard());
		_moveIterator = new ArrayIterator<Move>(_moves);
		_cardsLeft = (byte) tableau.cardsLeft();
		_treeScore = score;
		if (_parent != null) {
			try {
				_parent.addChild(this, score);
			} catch (Exception e) {
				e.printStackTrace();
			}
			_depth = (byte) (_parent._depth + 1);
		} else {
			_depth = 0;
		}
	}

	public ArrayIterator<Move> childMoves() {
		return _moveIterator;
	}

	public boolean hasMoves() {
		return _moveIterator.hasNext();
	}
	
	private void addChild(MoveTree c, int cs) {
		if (_childCount == _children.length) {
			expandChildren();
		}

		_children[_childCount++] = c;
		// this.updateScore(cs);
	}

	public Move move() {
		return _move;
	}

	public int score() {
		return _treeScore - (_depth * _depth);
	}

	public int depth() {
		return _depth;
	}

	public int cardsLeft() {
		return _cardsLeft;
	}

	public Tableau resultingTableau(Tableau initial) throws Exception {
		return resultingTableau(initial, 0);
	}

	public Tableau resultingTableau(Tableau initial, int startDepth) throws Exception {
		Tableau result = initial;
		Move[] moves = this.moves();
		if (startDepth > 0) {
			moves = Arrays.copyOfRange(moves, startDepth, moves.length);
		}
		int depth = startDepth;
		for (Move m : moves) {
			try {
				m.validate(result, depth++);
				result = Mover.move(result, m);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(initial);
				moves = this.moves();
				for (Move m2 : moves) {
					System.out.println(m2);
				}
				System.exit(-2);
			}
		}

		return result;
	}

	public int remove(MoveTree rt) {
		int cnt = 1;
		int remIdx = -1;

		for (int ii = 0; ii < _children.length; ++ii) {
			if (_children[ii] == rt) {
				remIdx = ii;
				break;
			}
		}

		if (remIdx >= 0) {
			_children[remIdx] = null;
			while (remIdx < _children.length - 1) {
				_children[remIdx] = _children[remIdx + 1];
				remIdx += 1;
			}
			_children[_children.length - 1] = null;
			_childCount -= 1;
		}

		_treeScore = Integer.MAX_VALUE;
		for (int ii = 0; ii < _childCount; ++ii) {
			MoveTree m = _children[ii];
			_treeScore = Math.min(m.score(), _treeScore);
		}

		rt._depth = -1;
		rt._cardsLeft = -1;

		if (_childCount == 0 && !_moveIterator.hasNext() && _parent != null) {
			cnt += _parent.remove(this);
			_parent = null;
		} /*
			 * else if (_parent != null) { _parent.updateScore(_treeScore); }
			 */

		return cnt;
	}

	public int remove(boolean force) {
		int cnt = 1;

		if (!force && (_childCount > 0 || _moveIterator.hasNext())) {
			return 0;
		}

		_depth = -1;
		_cardsLeft = -1;

		// transitively remove children first

		if (_children != null) {
			for (MoveTree kid : _children) {
				if (kid != null) {
					cnt += kid.remove(force);
				}
			}

			Arrays.setAll(_children, (what) -> null);
			_children = null;
		}

		if (_parent != null) {
			int result = _parent.remove(this) + cnt;
			_parent = null;
			return result;
		}

		return cnt;
	}

	protected void updateScore(int ns) {
		_treeScore = Math.min(_treeScore, ns);
		if (_treeScore == ns && _parent != null) {
			_parent.updateScore(_treeScore);
		}
	}

	public boolean hasChildren() {
		return _childCount > 0;
	}

	public int treeSize() {
		int count = 1;
		for (int ii = 0; ii < _childCount; ++ii) {
			MoveTree mt = _children[ii];
			count += mt.treeSize();
		}

		return count;
	}

	Iterator<MoveTree> childIterator() {
		return new ChildIterator(_children, _childCount);
	}

	public Iterator<MoveTree> iterator() {
		return new MTIterator(this);
	}

	public Move[] moves() throws Exception {
		ArrayList<Move> m = new ArrayList<Move>();
		this.addParentMoves(m);
		Move[] result = m.toArray(new Move[m.size()]);
		return result;
	}

	public MoveTree parent() {
		return _parent;
	}

	private void addParentMoves(ArrayList<Move> m) throws Exception {
		if (_parent != null) {
			_parent.addParentMoves(m);
		} else if (_depth != 0) {
			throw new Exception("early termination");
		}

		if (_move != null) {
			m.add(_move);
		}
	}

	void expandChildren() {
		MoveTree[] nt = Arrays.copyOf(_children, _children.length + CHILD_EXPAND_COUNT);
		_children = nt;
	}

	@Override
	public String toString() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');
		if (_parent != null) {
			sb.append('^');
		}
		sb.append(_childCount);
		sb.append(',');
		if (_move != null) {
			sb.append(_move.shortName());
			sb.append(',');
		}
		sb.append(_depth);
		sb.append(',');
		sb.append(this.score());
		sb.append(')');
		return sb.toString();
	}

	@Override
	public int compareTo(MoveTree o) {
		int priorityCompare = this.score() - o.score();
		return priorityCompare;
	}

	class MTIterator implements Iterator<MoveTree> {
		private final MoveTree _mt;
		private Stack<Iterator<MoveTree>> _parentIters = new Stack<Iterator<MoveTree>>();
		private Iterator<MoveTree> _childIter;
		private MoveTree _next = null;

		public MTIterator(MoveTree m) {
			_mt = m;
			_childIter = _mt.childIterator();
			_next = m;
		}

		private MoveTree getNextNext() {
			MoveTree next = null;
			// get next node, possibly ascending
			while (next == null) {
				if (_childIter.hasNext()) {
					next = _childIter.next();
					if (next.hasChildren()) {
						_parentIters.push(_childIter);
						_childIter = next.childIterator();
					}
				} else if (!_parentIters.isEmpty()) {
					_childIter = _parentIters.pop();
				} else {
					break;
				}
			}

			return next;
		}

		@Override
		public boolean hasNext() {
			return _next != null;
		}

		@Override
		public MoveTree next() {
			if (_next == null) {
				throw new NoSuchElementException();
			}

			MoveTree res = _next;
			_next = getNextNext();
			return res;
		}
	}

	class ChildIterator implements Iterator<MoveTree> {
		final MoveTree[] _children;
		final int _childCount;
		int nextIndex = 0;

		public ChildIterator(MoveTree[] kids, int count) {
			_children = kids;
			_childCount = count;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < _childCount;
		}

		@Override
		public MoveTree next() {
			return _children[nextIndex++];
		}

	}
}
