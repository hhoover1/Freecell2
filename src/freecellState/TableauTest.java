package freecellState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import deck.Card;
import deck.Card.Suit;
import freecellState.Location.Area;

public class TableauTest {
	private static final Location[] col2Locations = {
			new Location(Area.Tableau, 2, 0),
			new Location(Area.Tableau, 2, 1),
			new Location(Area.Tableau, 2, 2),
			new Location(Area.Tableau, 2, 3),
			new Location(Area.Tableau, 2, 4),
			new Location(Area.Tableau, 2, 5),
			new Location(Area.Tableau, 2, 6),
			new Location(Area.Tableau, 2, 7),
	};
	private static final Card[] cardStack = {
		Card.cardFrom("6D"),
		Card.cardFrom("7S"),
		Card.cardFrom("8D"),
		Card.cardFrom("9C"),
		Card.cardFrom("TH"),
		Card.cardFrom("JC"),
		Card.cardFrom("QD"),
		Card.cardFrom("KS")
	};
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
		assertEquals(150674, tableau.fitness());
	}

	@Test
	public final void testFitnessOrdered() {
		addOrderedStack();
		assertEquals(148646, tableau.fitness());
	}
	
	@Test
	public final void testFitnessReverseOrdered() {
		for (int ii = 0; ii < cardStack.length; ++ii) {
			tableau.put(col2Locations[ii], cardStack[ii]);
		}
		System.out.println(tableau);
		assertEquals(155736, tableau.fitness());
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
		assertEquals(1, tableau.fullyOrderedDepth(stack));
		addOrderedStack();
		stack = tableau.getTableauArray(2);
		assertEquals(8, tableau.fullyOrderedDepth(stack));
	}

	/**
	 * 
	 */
	private void addOrderedStack() {
		for (int ii = 0; ii < TableauTest.cardStack.length; ++ii) {
			tableau.put(TableauTest.col2Locations[ii], cardStack[col2Locations.length - ii - 1]);
		}
	}

	@Test
	public final void testPut() throws Exception {
		Location l = new Location(Area.Tableau, 3, 0);
		Card c1 = Card.cardFrom("JH");
		tableau.put(l, c1);
		assertEquals(c1, tableau.get(l));
		Card c2 = Card.cardFrom("TS");
		Location l2 = new Location(Area.Tableau, 3, 1);
		tableau.put(l2,  c2);
		assertEquals(c2, tableau.get(l2));
//		Card c3 = Card.cardFrom("QC");
//		tableau.put(l2,  c3);
//		Location l3 = new Location(Area.Tableau, 3, 2);
//		System.out.println(tableau);
//		assertEquals(c2, tableau.get(l3));
	}
	
	@Test
	public final void testTallestOrderedStack() {
		assertNotNull(tableau);
		addOrderedStack();
		assertEquals(8, tableau.tallestOrderedStack());
		Card c4 = Card.cardFrom("5S");
		Location l = new Location(Area.Tableau, 2, 8);
		tableau.put(l, c4);
		assertEquals(9, tableau.tallestOrderedStack());
	}
	
	@Test
	public final void testToString() {
		String s = tableau.toString();
		assertNotNull(s);
		System.out.println(s);
	}
	
	@Test
	public final void testHasTrappedCard() {
		assertTrue(tableau.hasTrappedCard());
	}
}
