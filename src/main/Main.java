package main;

import control.StagedDepthFirstSolver;

public class Main {
	private static final Arguments arguments = Arguments.arguments();
	private static StagedDepthFirstSolver solver;

	public static void main(String[] args) throws Exception {
		StagedDepthFirstSolver.setupArguments(arguments);
		
		if (args.length > 0) {
			arguments.parseArgs(args);
		}

		StagedDepthFirstSolver.initialize(arguments);
		solver = new StagedDepthFirstSolver(arguments);
		solver.runStagedDepthFirstSearch();
	}
}
