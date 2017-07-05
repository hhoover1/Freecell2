package deck;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import deck.Card.Suit;

public class CardStackTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testCardStack() {
		Card[] cards = new Card[1];
		cards[0] = Card.cardFrom("AS");
		CardSet nc = new CardStack(cards);
		assertNotNull(nc);
		try {
			nc = new CardStack(null);
			fail("did not throw on null array");
		} catch (Exception e) {
		}
		try {
			cards[0] = null;
			nc = new CardStack(cards);
			fail("did not throw on null card");
		} catch (Exception e) {
		}
		try {
			cards = new Card[0];
			nc = new CardStack(cards);
			fail("did not throw on zero-length array");
		} catch (Exception e) {
		}
	}

	@Test
	public final void testCardSetFrom() {
		final String test1 = "AS,JC";
		CardSet cs = CardStack.cardSetFrom(test1);
		assertNotNull(cs);
		assertEquals(2, cs.size());
	}

	@Test
	public final void testSuit() {
		final String test1 = "AS,JC";
		CardSet cs = CardStack.cardSetFrom(test1);
		assertNotNull(cs);
		assertEquals(Suit.Clubs, cs.suit());
	}

	@Test
	public final void testRank() {
		final String test1 = "AS,JC";
		CardSet cs = CardStack.cardSetFrom(test1);
		assertNotNull(cs);
		assertEquals(11, cs.rank());
	}

	@Test
	public final void testRankName() {
		final String test1 = "AS,JC";
		CardSet cs = CardStack.cardSetFrom(test1);
		assertNotNull(cs);
		assertEquals("Jack", cs.rankName());
	}

	@Test
	public final void testShortName() {
		final String test1 = "AS,JC,TH,5D";
		CardSet cs = CardStack.cardSetFrom(test1);
		assertNotNull(cs);
		assertEquals("(" + test1 + ")", cs.shortName());
	}

	@Test
	public final void testIsNextRankOf() {
		final String test1 = "5S,4H";
		CardSet cs = CardStack.cardSetFrom(test1);
		Card t1 = Card.cardFrom("3H");
		assertTrue(cs.isNextRankOf(t1));
	}

	@Test
	public final void testIsPreviousRankOf() {
		final String test1 = "5S,4H";
		CardSet cs = CardStack.cardSetFrom(test1);
		Card t1 = Card.cardFrom("5H");
		assertTrue(cs.isPreviousRankOf(t1));
	}

	@Test
	public final void testCanBePlacedOn() {
		final String test1 = "5S,4H";
		CardSet cs = CardStack.cardSetFrom(test1);
		Card t1 = Card.cardFrom("3C");
		Card t2 = Card.cardFrom("6D");
		CardSet cs1 = CardStack.cardSetFrom("8D,7C,6D");
		CardSet cs2 = CardStack.cardSetFrom("3S,2H");
		assertTrue(t1.canBePlacedOn(cs));
		assertTrue(cs.canBePlacedOn(t2));
		assertFalse(cs.canBePlacedOn(t1));
		assertFalse(t2.canBePlacedOn(cs));
		assertTrue(cs.canBePlacedOn(cs1));
		assertFalse(cs1.canBePlacedOn(cs));
		assertTrue(cs2.canBePlacedOn(cs));
		assertFalse(cs.canBePlacedOn(cs2));
	}

	@Test
	public final void testTop() {
		final String test1 = "5S,4H";
		CardSet cs = CardStack.cardSetFrom(test1);
		final Card tc1 = Card.cardFrom("4H");
		assertEquals(tc1, cs.top());
	}

	@Test
	public final void testBottom() {
		final String test1 = "5S,4H,3C";
		CardSet cs = CardStack.cardSetFrom(test1);
		final Card tc1 = Card.cardFrom("5S");
		assertEquals(tc1, cs.bottom());
	}

	@Test
	public final void testSize() {
		final String test1 = "5S,4H";
		CardSet cs = CardStack.cardSetFrom(test1);
		assertEquals(2, cs.size());
	}

	@Test
	public final void testCardAt() throws Exception {
		final String test1 = "8D,7S,6H,5C";
		CardSet cs = CardSet.cardsFrom(test1);
		
	}
	
	@Test
	public final void testSplit() throws Exception {
		final String test1 = "5S,4H,3C";
		CardSet cs = CardStack.cardSetFrom(test1);
		CardSet[] tcs = cs.split(1);
		assertNotNull(tcs);
		assertEquals(2, tcs.length);
		assertNotNull(tcs[0]);
		assertNotNull(tcs[1]);
		assertEquals(Card.cardFrom("3C"), tcs[0]);
		assertEquals(1, tcs[0].size());
		assertEquals(2, tcs[1].size());
		
	}

}
