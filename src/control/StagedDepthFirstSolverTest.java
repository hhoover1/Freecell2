package control;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class StagedDepthFirstSolverTest {
	private static final long EXPECTED_COUNT = 100;
	private StagedDepthFirstSolver _solver = null;
	
	@Before
	public void setUp() throws Exception {
		_solver = new StagedDepthFirstSolver();
	}

	@Test
	public final void testVeryShortTree() {
		_solver.runStagedDepthFirstSearch(3, 3);
		assertEquals(EXPECTED_COUNT, _solver.count());
	}
}
