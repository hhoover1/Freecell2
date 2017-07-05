package deck;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import deck.Card.Suit;

public class CardSetTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testSuit() {
		CardSet cs = Card.cardFrom("AS");
		assertEquals(Suit.Spades, cs.suit());
		CardSet cs2;
		try {
			cs2 = CardSet.cardsFrom("3S,2D");
			assertEquals(Suit.Diamonds, cs2.suit());
		} catch (Exception e) {
			fail("Unexpected Exception");
		}
	}

	@Test
	public final void testRank() {
		CardSet cs = Card.cardFrom("AS");
		assertEquals(1, cs.rank());
		CardSet cs2;
		try {
			cs2 = CardSet.cardsFrom("3S,2D");
			assertEquals(2, cs2.rank());
		} catch (Exception e) {
			fail("Unexpected Exception");
		}
	}

	@Test
	public final void testRankName() {
		CardSet cs = Card.cardFrom("AS");
		assertEquals("Ace", cs.rankName());
		CardSet cs2;
		try {
			cs2 = CardSet.cardsFrom("3S,2D");
			assertEquals("2", cs2.rankName());
		} catch (Exception e) {
			fail("Unexpected Exception");
		}
	}

	@Test
	public final void testShortName() {
		CardSet cs = Card.cardFrom("AS");
		assertEquals("AS", cs.shortName());
		CardSet cs2;
		try {
			cs2 = CardSet.cardsFrom("3S,2D");
			assertEquals("(3S,2D)", cs2.shortName());
		} catch (Exception e) {
			fail("Unexpected Exception");
		}
	}

	@Test
	public final void testIsNextRankOf() {
		CardSet cs1 = Card.cardFrom("7S");
		CardSet cs2 = CardStack.cardSetFrom("9S,8S");
		assertTrue(cs2.isNextRankOf(cs1));
		assertFalse(cs1.isNextRankOf(cs2));
		assertFalse(cs1.isNextRankOf(cs1));
	}

	@Test
	public final void testIsPreviousRankOf() {
		CardSet cs1 = Card.cardFrom("7S");
		CardSet cs2 = CardStack.cardSetFrom("9S,8S");
		assertTrue(cs1.isPreviousRankOf(cs2));
		assertFalse(cs2.isPreviousRankOf(cs1));
		assertFalse(cs1.isPreviousRankOf(cs1));
	}

	@Test
	public final void testCanBePlacedOn() {
		CardSet cs1 = Card.cardFrom("7H");
		CardSet cs2 = CardStack.cardSetFrom("9D,8S");
		assertTrue(cs1.canBePlacedOn(cs2));
		assertFalse(cs2.canBePlacedOn(cs1));
	}

	@Test
	public final void testTop() {
		CardSet cs1 = CardStack.cardSetFrom("9S,8S");
		assertEquals(Card.cardFrom("8S"), cs1.top());
	}

	@Test
	public final void testBottom() {
		CardSet cs1 = CardStack.cardSetFrom("9S,8S");
		assertEquals(Card.cardFrom("9S"), cs1.bottom());
	}

	@Test
	public final void testSize() {
		CardSet cs1 = Card.cardFrom("7H");
		CardSet cs2 = CardStack.cardSetFrom("9D,8S");
		assertEquals(1, cs1.size());
		assertEquals(2, cs2.size());
	}

	@Test
	public final void testSplit() {
		try {
			CardSet cs1 = CardSet.cardsFrom("JS");
			CardSet[] split1 = cs1.split(0);
			assertNotNull(split1);
			assertEquals(2, split1.length);
			assertEquals(null, split1[0]);
			assertEquals(cs1, split1[1]);

			CardSet cs2 = CardSet.cardsFrom("4H,3C,2D");
			split1 = cs2.split(1);
			assertNotNull(split1);
			assertEquals(2, split1.length);
			assertEquals(Card.cardFrom("2D"), split1[0]);
			assertEquals(2, split1[1].size());
		} catch (Exception e) {
			fail("unexpected throw");
		}
	}

	@Test
	public final void testCardsFrom() throws Exception {
		CardSet cs1 = CardSet.cardsFrom("TC,9C,8C,7C,3C");
		assertNotNull(cs1);
		assertEquals(5, cs1.size());
		assertEquals(CardSet.cardsFrom("3C"), cs1.top());
		assertEquals(CardSet.cardsFrom("TC"), cs1.bottom());
	}

}
