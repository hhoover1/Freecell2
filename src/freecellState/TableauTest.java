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
		c = tableau.getTableau(0, 2);
		assertNotNull(c);
		assertEquals(Card.cardFrom("3D"), c);
		assertNotNull(tableau.getTableau(1, 0));
	}

	@Test
	public final void testGet() throws Exception {
		assertNotNull(tableau);
		assertNotNull(tableau.get(new Location(Area.Freecell, 1, 0)));
	}

	@Test
	public final void testFitnessBasic() {
		assertNotNull(tableau);
		assertEquals(21374, tableau.fitness());
	}

	@Test
	public final void testStackCardScore() {
		assertNotNull(tableau);
		for (int ii = 2; ii < 8; ++ii) {
			assertEquals(0, tableau.stackCardScore(ii));
		}
		assertEquals(19, tableau.stackCardScore(0));
		assertEquals(7, tableau.stackCardScore(1));
		Location l = new Location(Area.Tableau, 2, 0);
		Card c = Card.cardFrom("5C");
		tableau.put(l, c);
		//System.out.println(tableau);
		assertEquals(8, tableau.stackCardScore(2));
	}

	@Test
	public final void testEmptyTableauColumns() {
		assertNotNull(tableau);
		assertEquals(6, tableau.emptyTableauColumns());
	}
	
	@Test
	public final void testFullyOrderedDepth() {
		assertNotNull(tableau);
		Card[] stack = tableau.getTableauArray(0);
		assertEquals(0, tableau.fullyOrderedDepth(stack));
		stack = tableau.getTableauArray(2);
		assertEquals(0, tableau.fullyOrderedDepth(stack));
		addOrderedStack();
		stack = tableau.getTableauArray(2);
		assertEquals(2, tableau.fullyOrderedDepth(stack));
		System.out.println(tableau);
	}

	/**
	 * 
	 */
	private void addOrderedStack() {
		Location l = new Location(Area.Tableau, 2, 0);
		Card c1 = Card.cardFrom("JD");
		tableau.put(l,  c1);
		l = new Location(Area.Tableau, 2, 1);
		Card c2 = Card.cardFrom("TC");
		tableau.put(l, c2);
		l = new Location(Area.Tableau, 2, 2);
		Card c3 = Card.cardFrom("9H");
		tableau.put(l, c3);
	}

	@Test
	public final void testTallestOrderedStack() {
		assertNotNull(tableau);
		addOrderedStack();
		assertEquals(3, tableau.tallestOrderedStack());
		Card c4 = Card.cardFrom("8S");
		Location l = new Location(Area.Tableau, 2, 3);
		tableau.put(l, c4);
		assertEquals(4, tableau.tallestOrderedStack());
	}
	
	@Test
	public final void testToString() {
		String s = tableau.toString();
		assertNotNull(s);
		System.out.println(s);
	}
}
