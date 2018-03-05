package freecellState;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import deck.Card;
import deck.Card.Suit;
import freecellState.Location.Area;

public class MoverTest {
	private static final Card[] found = new Card[4];
	private static final Card[] free = new Card[4];
	private static Card[][] cardTab = new Card[8][];
	private static Tableau tableau;
	private static final Location l1 = new Location(Area.Tableau, 0, 0, -1);
	private static final Location l2 = new Location(Area.Freecell, 0, 0, -1);
	private static final String deck38Str = "2H,JS,KC,4C,3D,AH,QC,AS,8H,QH,6S,3C,6C,4H,4S,TS,5C,5D,7C,6H,4D,7D,KH,KD,5S,5H,3H,9D,7H,JC,KS,9C,8C,8D,JH,2D,9H,JD,QS,QD,6D,8S,2C,TH,7S,TC,AC,9S,AD,TD,2S,3S";
	private static final Location from1 = new Location(Area.Tableau, 0, 0, -1);
	private static final Location to1 = new Location(Area.Foundation, 2, 0, -1);
	private static final Move m1 = new Move(from1, to1);
	
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
		tableau = new Tableau(found, free, TableauStack.fromCardArray(cardTab));
	}


	@Test
	public final void testMove() throws Exception {
		assertNotNull(tableau);
		System.out.println(tableau);
		Move m1 = new Move(l1, l2);
		Tableau newT = Mover.move(tableau, m1);
		assertNotNull(newT.getFree(0));
		try {
			Card c = newT.getCardFromTableau(0, 2);
			assertEquals(Card.cardFrom("TS"), c);
		} catch (Exception e) {
			// success
		}
		System.out.println(newT);
	}

	@Test
	public final void testFullDeckMove() throws Exception {
		Tableau testT = Tableau.fromString(deck38Str);
		System.out.println(testT);
		Tableau newT = Mover.move(testT, m1);
		System.out.println(newT);
		assertNotNull(newT);
		assertEquals(Card.cardFrom("AD"), newT.foundation(2));
	}
	
	@Test
	public final void testIsWin_Win() {
		Card[] found = {Card.cardFrom("KH"), Card.cardFrom("KC"), Card.cardFrom("KD"), Card.cardFrom("KS") };
		Card[][] tab = new Card[Tableau.TABLEAU_SIZE][];
		Card[] free = new Card[Tableau.FREECELL_COUNT];
		for (int ii = 0; ii < tab.length; ++ii) {
			tab[ii] = new Card[0];
		}
		Tableau winTab = new Tableau(found, free, TableauStack.fromCardArray(tab));
		assertNotNull(winTab);
		assertFalse(winTab.hasTrappedCard());
		assertTrue(Mover.isWin(winTab));
	}
	
	@Test
	public final void testIsWin_NotWin() {
		Card[] found = {Card.cardFrom("KH"), Card.cardFrom("QC"), Card.cardFrom("KD"), Card.cardFrom("KS") };
		Card[][] tab = new Card[Tableau.TABLEAU_SIZE][];
		Card[] free = new Card[Tableau.FREECELL_COUNT];
		for (int ii = 1; ii < tab.length; ++ii) {
			tab[ii] = new Card[0];
		}
		Card[] holdOut = { Card.cardFrom("KC") };
		tab[0] = holdOut;
		
		Tableau winTab = new Tableau(found, free, TableauStack.fromCardArray(tab));
		assertNotNull(winTab);
		assertFalse(winTab.hasTrappedCard());
		assertFalse(Mover.isWin(winTab));		
	}
}
