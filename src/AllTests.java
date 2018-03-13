import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ deck.AllTests.class, explore.AllTests.class, freecellState.AllTests.class, main.ArgumentsTest.class,
		control.StagedDepthFirstSolverTest.class })

public class AllTests {

}
