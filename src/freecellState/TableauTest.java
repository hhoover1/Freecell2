package freecellState;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import deck.Card;
import deck.Card.Suit;
import freecellState.Location.Area;

public class TableauTest {
	private static final Card[] found = new Card[4];
	private static final Card[] free = new Card[4];
	private static Card[][] cardTab = new Card[8][];
	private static Tableau tableau;

	@Before
	public void setUp() throws Exception {
		found[0] = new Card(Suit.Hearts, 1);
		free[1] = new Card(Suit.Clubs, 12);
		Card[] t0 = new Card[3];
		t0[0] = new Card(Suit.Diamonds, 3);
		t0[1] = new Card(Suit.Diamonds, 7);
		t0[2] = new Card(Suit.Spades, 10);
		cardTab[0] = t0;
		Card[] t1 = new Card[1];
		t1[0] = new Card(Suit.Clubs, 6);
		cardTab[1] = t1;
		for (int idx = 2; idx < cardTab.length; ++idx) {
			cardTab[idx] = new Card[0];
		}
		tableau = new Tableau(found, free, cardTab);
	}

	@Test
	public final void testTableau() throws Exception {
		assertNotNull(tableau);
		assertNotNull(tableau.get(new Location(Area.Foundation, 0, 0)));
		assertNull(tableau.get(new Location(Area.Foundation, 1, 0)));
	}

	@Test
	public final void testGetFound() {
		assertNotNull(tableau);
		assertNotNull(tableau.getFound(Suit.Hearts));
		assertNull(tableau.getFound(Suit.Clubs));
	}

	@Test
	public final void testGetFree() {
		assertNotNull(tableau);
		assertNull(tableau.getFree(0));
		assertNotNull(tableau.getFree(1));
		assertNull(tableau.getFree(2));
		assertNull(tableau.getFree(3));
	}

	@Test
	public final void testGetTableau() {
		assertNotNull(tableau);
		Card c = tableau.getTableau(0, 0);
		assertNotNull(c);
		assertEquals(Card.cardFrom("TS"), c);
		c = tableau.getTableau(0, 1);
		assertNotNull(c);
		assertEquals(Card.cardFrom("7D"), c);
		c = tableau.getTableau(0,  2);
		assertNotNull(c);
		assertEquals(Card.cardFrom("3D"), c);
		assertNotNull(tableau.getTableau(1,  0));
	}

	@Test
	public final void testGet() throws Exception {
		assertNotNull(tableau);
		assertNotNull(tableau.get(new Location(Area.Freecell, 1, 0)));
	}

	@Test
	public final void testToString() {
		String s = tableau.toString();
		assertNotNull(s);
		System.out.println(s);
	}

}
