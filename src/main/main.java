package main;

import control.StagedDepthFirstSolver;

public class main {
	private static final Arguments arguments = new Arguments();
	private static StagedDepthFirstSolver solver;

	public static void main(String[] args) {
		StagedDepthFirstSolver.setupArguments(arguments);
		
		if (args.length > 0) {
			arguments.parseArgs(args);
		}

		StagedDepthFirstSolver.initialize(arguments);
		solver = new StagedDepthFirstSolver(arguments);
		solver.runStagedDepthFirstSearch();
	}
}
