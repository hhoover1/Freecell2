package deck;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CardTest.class, DeckTest.class, CardStackTest.class, CardSetTest.class })
public class AllTests {

}
