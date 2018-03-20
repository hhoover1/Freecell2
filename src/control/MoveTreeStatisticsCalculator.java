package control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

import explore.MoveTree;
import freecellState.Tableau;
import main.Arguments;

public class MoveTreeStatisticsCalculator {
	private final int MAX_EXPLORE_DEPTH = StagedDepthFirstSolver.MAX_EXPLORE_DEPTH;
	Tableau _startingTableau;
	ArrayList<MoveTreeStatistic> _statsByDepthTree = new ArrayList<MoveTreeStatistic>(MAX_EXPLORE_DEPTH);
	ArrayList<MoveTreeStatistic> _statsByDepthQueue = new ArrayList<MoveTreeStatistic>(MAX_EXPLORE_DEPTH);
	MoveTreeStatistic _statsTree = new MoveTreeStatistic();
	MoveTreeStatistic _statsQueue = new MoveTreeStatistic();
	Arguments arguments;
	boolean _calculated = false;

	public MoveTreeStatisticsCalculator(Tableau start, Arguments args) {
		_startingTableau = start;
		arguments = args;
		for (int ii = 0; ii < MAX_EXPLORE_DEPTH; ++ii) {
			_statsByDepthTree.add(new MoveTreeStatistic());
			_statsByDepthQueue.add(new MoveTreeStatistic());
		}
		_calculated = false;
	}

	public void calculateStatistics(MoveTree root, Queue<MoveTree> queued) {
		if (!_calculated) {
			System.out.println("Starting statistics calculations");
			processRoot(root);
			System.out.println("done root, starting queue");
			processQueue(queued);
			System.out.println("Done statistics calculations");
			_calculated = true;
		}
	}

	public MoveTreeStatistic globalTreeStats() {
		return _statsTree;
	}

	public MoveTreeStatistic globalQueueStats() {
		return _statsQueue;
	}

	public int treeDepth() {
		for (int ii = 0; ii < _statsByDepthTree.size(); ++ii) {
			if (!_statsByDepthTree.get(ii).validData()) {
				return ii;
			}
		}

		return _statsByDepthTree.size();
	}

	public int queueDepth() {
		for (int ii = _statsByDepthQueue.size() - 1; ii >= 0; --ii) {
			if (_statsByDepthQueue.get(ii).validData()) {
				return ii + 1;
			}
		}

		return _statsByDepthQueue.size();
	}

	public MoveTreeStatistic treeStatAtDepth(int depth) {
		return _statsByDepthTree.get(depth);
	}

	public MoveTreeStatistic queueStatAtDepth(int depth) {
		return _statsByDepthQueue.get(depth);
	}

	private void processRoot(MoveTree root) {
		final ArrayList<MoveTreeStatistic> treeCollection = _statsByDepthTree;
		Iterator<MoveTree> iter = root.iterator();
		processCollection(iter, _statsTree, treeCollection);
	}

	/**
	 * @param root
	 * @param treeCollection
	 */
	private void processQueue(Queue<MoveTree> queued) {
		final ArrayList<MoveTreeStatistic> treeCollection = _statsByDepthQueue;
		Iterator<MoveTree> iter = queued.iterator();
		processCollection(iter, _statsQueue, treeCollection);
	}

	private void processCollection(Iterator<MoveTree> iter, MoveTreeStatistic global,
			final ArrayList<MoveTreeStatistic> treeCollection) {
		int count = 0;
		int dotInterval = arguments.flushDotInterval;
		while (iter.hasNext()) {
			MoveTree m = iter.next();
			int d = m.depth();
			global.addTree();
			global.addScore(m.score());
			global.addRemaining(m.cardsLeft());
			MoveTreeStatistic mts = treeCollection.get(d);
			mts.addTree();
			mts.addScore(m.score());
			mts.addRemaining(m.cardsLeft());
			if (++count % dotInterval == 0) {
				System.out.print(".");
			}
		}
	}

	public class MoveTreeStatistic {
		long _movetreesInGroup;
		long _movetreeSumScore;
		long _movetreeSumRemainingMoves;
		int _minScore = Integer.MAX_VALUE;
		int _maxScore = Integer.MIN_VALUE;
		int _minRemaining = Integer.MAX_VALUE;
		int _maxRemaining = Integer.MIN_VALUE;

		MoveTreeStatistic() {
			_movetreesInGroup = 0;
			_movetreeSumScore = 0;
			_movetreeSumRemainingMoves = 0;
		}

		public boolean validData() {
			return _minScore != Integer.MAX_VALUE;
		}

		public void addTree() {
			_movetreesInGroup += 1;
		}

		public void addScore(int score) {
			_movetreeSumScore += score;
			_minScore = Math.min(_minScore, score);
			_maxScore = Math.max(_maxScore, score);
		}

		public void addRemaining(int remaining) {
			_movetreeSumRemainingMoves += remaining;
			_minRemaining = Math.min(_minRemaining, remaining);
			_maxRemaining = Math.max(_maxRemaining, remaining);
		}

		public long count() {
			return _movetreesInGroup;
		}

		public String formatMinMax(String format) {
			return String.format(format, _minScore, _maxScore, _minRemaining, _maxRemaining);
		}

		public float averageScore() {
			double avgScore = (double) _movetreeSumScore / _movetreesInGroup;
			return (float) avgScore;
		}

		public float averageRemainingMoves() {
			double avgRemain = (double) _movetreeSumRemainingMoves / _movetreesInGroup;
			return (float) avgRemain;
		}

	}
}
