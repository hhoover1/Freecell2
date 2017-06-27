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
	private static final int DEPTH_BASE = 100;
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
	
	// special constructor for testing
	MoveTree(MoveTree p, Move m, int score, int depth) {
		_parent = p;
		_move = m;
		_treeScore = score;
		_depth = depth;
	}

	private void addChild(MoveTree c, int cs) {
		_children.add(c);
//		this.updateScore(cs);
	}

	public Move move() {
		return _move;
	}

	public int score() {
		return _treeScore - depthFunction();
	}

	public int depth() {
		return _depth;
	}

	public Tableau resultingTableau(Tableau initial) {
		return resultingTableau(initial, 0);
	}

	public Tableau resultingTableau(Tableau initial, int startDepth) {
		Tableau result = initial;
		Move[] moves = this.moves();
		if (startDepth > 0) {
			moves = Arrays.copyOfRange(moves, startDepth, moves.length);
		}
		for (Move m : moves) {
			try {
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
		_children.remove(rt);
		_treeScore = Integer.MAX_VALUE;
		for (MoveTree m : _children) {
			_treeScore = Math.min(m.score(), _treeScore);
		}

		if (_children.isEmpty() && _parent != null) {
			cnt += _parent.remove(this);
		} /*else if (_parent != null) {
			_parent.updateScore(_treeScore);
		}*/
		
		return cnt;
	}

	public int remove() {
		int cnt = 0;

		// transitively remove children first
		if (!_children.isEmpty()) {
			MoveTree[] kids = _children.toArray(new MoveTree[_children.size()]);
			for (MoveTree c : kids) {
				cnt += c.remove();
			}
		}

		if (_parent != null) {
			return _parent.remove(this) + cnt;
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
		return !_children.isEmpty();
	}

	public int treeSize() {
		int count = 1;
		for (MoveTree mt : _children) {
			count += mt.treeSize(); 
		}
		
		return count;
	}
	
	Iterator<MoveTree> childIterator() {
		return _children.iterator();
	}

	public Iterator<MoveTree> iterator() {
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

		if (_move != null) {
			m.add(_move);
		}
	}
	
	private int depthFunction() {
		int adjustedDepth = (DEPTH_BASE - _depth);
		int result = (DEPTH_BASE*DEPTH_BASE) - (adjustedDepth * adjustedDepth);
		// result *= Math.signum(-adjustedDepth);
		return result;
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
		sb.append(this.score());
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

	@Override
	public int compareTo(MoveTree o) {
		int priorityCompare = this.score() - o.score();
		return priorityCompare;
	}
}
