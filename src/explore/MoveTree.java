package explore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import freecellState.Move;

public class MoveTree {
	private final MoveTree _parent;
	private final Move _move;
	private ArrayList<MoveTree> _children = new ArrayList<MoveTree>();
	private int _depth;
	private int _treeScore;

	public MoveTree() {
		_parent = null;
		_move = null;
		_treeScore = Integer.MAX_VALUE;
		_depth = 0;
	}

	public MoveTree(MoveTree p, Move m, int score) {
		_parent = p;
		_move = m;
		_treeScore = score;
		if (_parent != null) {
			_parent.addChild(this, score);
			_depth = _parent._depth + 1;
		} else {
			_depth = 0;
		}
	}

	private void addChild(MoveTree c, int cs) {
		_children.add(c);
		this.updateScore(cs);
	}

	public Move move() {
		return _move;
	}

	public int score() {
		return _treeScore;
	}

	public void setScore(int ns) {
		_treeScore = ns;
	}
	
	public int depth() {
		return _depth;
	}

	public void remove(MoveTree rt) {
		_children.remove(rt);
		_treeScore = Integer.MAX_VALUE;
		for (MoveTree m : _children) {
			_treeScore = Math.min(m.score(), _treeScore);
		}

		if (_parent != null) {
			_parent.updateScore(_treeScore);
		}

		if (_children.isEmpty() && _parent != null) {
			_parent.remove(this);
		}
	}

	public void remove() {
		if (_parent != null) {
			_parent.remove(this);
		}
	}

	public void updateScore(int ns) {
		_treeScore = Math.min(_treeScore, ns);
		if (_treeScore == ns && _parent != null) {
			_parent.updateScore(_treeScore);
		}
	}

	public boolean hasChildren() {
		return !_children.isEmpty();
	}

	Iterator<MoveTree> childIterator() {
		return _children.iterator();
	}

	Iterator<MoveTree> iterator() {
		return new MTIterator(this);
	}

	public Move[] moves() {
		ArrayList<Move> m = new ArrayList<Move>();
		this.addParentMoves(m);
		Move[] result = m.toArray(new Move[0]);
		return result;
	}

	public MoveTree parent() {
		return _parent;
	}

	private void addParentMoves(ArrayList<Move> m) {
		if (_parent != null) {
			_parent.addParentMoves(m);
		}

		m.add(_move);
	}

	@Override
	public String toString() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');
		if (_parent != null) {
			sb.append('^');
		}
		sb.append(_children.size());
		sb.append(',');
		if (_move != null) {
			sb.append(_move.shortName());
			sb.append(',');
		}
		sb.append(_depth);
		sb.append(',');
		sb.append(_treeScore);
		sb.append(')');
		return sb.toString();
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
}
