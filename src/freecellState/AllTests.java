package freecellState;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LocationTest.class, MoveTest.class, MoverTest.class, TableauTest.class, TableauHashTest.class })
public class AllTests {

}
