package control;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import main.Arguments;

public class StagedDepthFirstSolverTest {
	private static final long EXPECTED_COUNT = 8333;
	private StagedDepthFirstSolver _solver = null;
	
	@Before
	public void setUp() throws Exception {
		Arguments arguments = new Arguments();
		arguments.validation = false;
		StagedDepthFirstSolver.setupArguments(arguments);
		StagedDepthFirstSolver.initialize(arguments);
		arguments.maxExploreDepth = 3;
		arguments.intermediateDepth = 3;
		_solver = new StagedDepthFirstSolver(arguments);
	}

	@Test
	public final void testVeryShortTree() {
		_solver.runStagedDepthFirstSearch();
		assertEquals(EXPECTED_COUNT, _solver.count());
	}
}
