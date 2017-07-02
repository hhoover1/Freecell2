package deck;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import deck.Card.Suit;

public class CardTest {
	private static final Card[] heartsList = {
			Card.cardFrom("AH"),
			Card.cardFrom("2H"),
			Card.cardFrom("3H"),
			Card.cardFrom("4H"),
			Card.cardFrom("5H"),
			Card.cardFrom("6H"),
			Card.cardFrom("7H"),
			Card.cardFrom("8H"),
			Card.cardFrom("9H"),
			Card.cardFrom("TH"),
			Card.cardFrom("JH"),
			Card.cardFrom("QH"),
			Card.cardFrom("KH")
	};
	
	@Test
	public final void testHashCode() throws Exception {
		Card c = new Card(Suit.Hearts, 7);
		int hc = c.hashCode();
		assertEquals(7, hc);
		Deck dk = new Deck();
		ArrayList<Integer> hashes = new ArrayList<Integer>();
		Deal d = new Deal(dk);
		while (!d.isEmpty()) {
			c = d.next();
			hc = c.hashCode();
			hashes.add(hc);
		}
		hashes.sort(null);
		for (int ii = 0; ii < hashes.size() - 1; ++ii) {
			if (hashes.get(ii) == hashes.get(ii + 1)) {
				fail("found 2 hashes the same!");
			}
		}
	}

	@Test
	public final void testCardFrom() {
		Card c = Card.cardFrom("AS");
		assertEquals(Suit.Spades, c.suit());
		assertEquals(1, c.rank());
	}

	@Test
	public final void testCard() {
		Card c = new Card(Suit.Diamonds, 8);
		assertNotNull(c);
		assertEquals(Suit.Diamonds, c.suit());
		assertEquals(8, c.rank());
	}

	@Test
	public final void testRankName() {
		Card c = new Card(Suit.Diamonds, 8);
		assertNotNull(c);
		assertEquals("8", c.rankName());
	}

	@Test
	public final void testShortName() {
		Card c = new Card(Suit.Diamonds, 8);
		assertNotNull(c);
		assertEquals("8D", c.shortName());
	}

	@Test
	public final void testEqualsObject() {
		Card c = new Card(Suit.Diamonds, 8);
		assertNotNull(c);
		assertFalse(c.equals(null));
		assertTrue(c.equals(c));
		assertTrue(c.equals(new Card(Suit.Diamonds, 8)));
		assertFalse(c.equals(new ArrayList<Card>()));
	}

	@Test
	public final void testToString() {
		Card c = new Card(Suit.Diamonds, 8);
		assertEquals("Card(8 of Diamonds)", c.toString());
	}

	@Test
	public final void testRankFromChar() {
		assertEquals(1, Card.rankFromChar("A"));
		assertEquals(2, Card.rankFromChar("2"));
		assertEquals(9, Card.rankFromChar("9"));
		assertEquals(10, Card.rankFromChar("T"));
		assertEquals(12, Card.rankFromChar("Q"));
		assertEquals(0, Card.rankFromChar("K"));
	}

	@Test
	public final void testSuitFromChar() {
		assertEquals(Suit.Clubs, Card.suitFromChar("C"));
		assertEquals(Suit.Diamonds, Card.suitFromChar("D"));
		assertEquals(Suit.Hearts, Card.suitFromChar("H"));
		assertEquals(Suit.Spades, Card.suitFromChar("S"));
	}

	@Test
	public final void testIsNextRankOf() {
		Card t1 = new Card(Suit.Hearts, 1);
		assertFalse(t1.isNextRankOf(new Card(Suit.Hearts, 0)));
		t1 = new Card(Suit.Diamonds, 2);
		assertTrue(t1.isNextRankOf(new Card(Suit.Diamonds, 1)));
		for (int ii = 1; ii < heartsList.length; ++ii) {
			Card c1 = heartsList[ii];
			Card c2 = heartsList[ii - 1];
			assertTrue(c1.isNextRankOf(c2));
			assertFalse(c2.isNextRankOf(c1));
		}
		assertFalse(heartsList[0].isNextRankOf(heartsList[12]));
	}

	@Test
	public final void testIsPreviousRankOf() {
		Card t1 = new Card(Suit.Hearts, 0);
		assertFalse(t1.isPreviousRankOf(new Card(Suit.Hearts, 1)));
		t1 = new Card(Suit.Diamonds, 3);
		assertTrue(t1.isPreviousRankOf(new Card(Suit.Clubs, 4)));
	}

}
