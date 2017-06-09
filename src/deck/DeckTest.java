package deck;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class DeckTest {
	private static final int SUIT_SIZE = Deck.DECKSIZE / (Card.Suit.values().length);
	public static final String deckString = "AS,7C,KS,AC,8C,6S,AH,9D,7H,3C,AD,KD,9C,KC,8D,6H,TH,3D,TS,4D,QS,QC,4H,6C,TC,JH,JC,8S,JS,5C,2S,9H,5S,7S,JD,4C,3H,TD,3S,6D,4S,2C,7D,QH,2D,2H,8H,5D,QD,5H,9S,KH";
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void basicTest() throws Exception {
		Deal d = new Deal(new Deck());
		int dcount = 0;
		while (!d.isEmpty()) {
			Card c = d.next();
			int ccount = c.suit().ordinal() * SUIT_SIZE + c.rank();
			assertEquals(ccount, dcount);
			dcount += 1;
		}
		assertEquals(dcount, Deck.DECKSIZE);
	}

	@Test
	public final void shuffleTest() throws Exception {
		Deck dk1 = new Deck();
		Deck dk2 = new Deck();
		Deck dk3 = new Deck();
		dk2.shuffle();
		dk3.shuffle();
		Deal d1 = new Deal(dk1);
		Deal d2 = new Deal(dk2);
		Deal d3 = new Deal(dk3);
		int s12 = 0;
		int s23 = 0;
		int s13 = 0;
		for (int ii = 0; ii < Deck.DECKSIZE; ++ii) {
			Card c1 = d1.next();
			Card c2 = d2.next();
			Card c3 = d3.next();
			if (c1.equals(c2)) {
				s12 += 1;
			}
			if (c2.equals(c3)) {
				s23 += 1;
			}
			if (c1.equals(c3)) {
				s13 += 1;
			}
		}
		
		if (s12 > 4 || s23 > 4 || s13 > 4) {
			fail("too many similars");
		}
		
		System.out.println(String.format("sames: %d, %d, %d", s12, s23, s13));
	}
	
	@Test
	public final void deckFromTest() {
		Deck d = Deck.deckFrom(deckString);
		assertNotNull(d);
		assertTrue(d.size() > 0);
		System.out.println(d);
	}
}
