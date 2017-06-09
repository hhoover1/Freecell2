package explore;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MoveCalculatorTest.class, MoveTreeTest.class, TableauMoveIteratorTest.class })
public class AllTests {

}
