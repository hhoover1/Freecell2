import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({control.StagedDepthFirstSolverTest.class, deck.AllTests.class, explore.AllTests.class, freecellState.AllTests.class, main.ArgumentsTest.class })
public class AllTests {

}
