package explore;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import deck.Card;
import freecellState.Move;
import freecellState.Tableau;

public class TableauMoveIteratorTest {
	@SuppressWarnings("unused")
	private static String testDeck1 = "KH,KS,KC,KD";
	private static Card[] found1 = new Card[] { Card.cardFrom("QH"), Card.cardFrom("QC"), Card.cardFrom("QD"),
			Card.cardFrom("QS") };
	private static Card[][] tableau1 = { { Card.cardFrom("KC") }, { Card.cardFrom("KD") }, { Card.cardFrom("KH") },
			{ Card.cardFrom("KS") }, {}, {}, {}, {} };

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testTableauMoveIteratorTableauInt() {
		Tableau t = new Tableau(found1, new Card[4], tableau1);
		System.out.println(t);
		TableauMoveIterator tmi = new TableauMoveIterator(t, 10);
		assertNotNull(tmi);
		assertTrue(tmi.hasNext());
		ArrayIterator<Move> moves = tmi.moves();
		while (moves.hasNext()) {
			System.out.println(moves.next());
		}
	}

	@Test
	public final void testTableauMoveIteratorTableauMoveTreeInt() {
		Tableau t = new Tableau(found1, new Card[Tableau.FREECELL_COUNT], tableau1);
		MoveTree m = new MoveTree();
		TableauMoveIterator tmi = new TableauMoveIterator(t, m, 120, 0);
		assertNotNull(tmi);
		assertTrue(tmi.hasNext());
	}

	@Test
	public final void testTreeRoot() {
		Tableau t = new Tableau(found1, new Card[Tableau.FREECELL_COUNT], tableau1);
		MoveTree m = new MoveTree();
		TableauMoveIterator tmi = new TableauMoveIterator(t, m, 120, 0);
		assertNotNull(tmi);
		assertNotNull(tmi.treeRoot());
	}

	@Test
	public final void testHasNext() {
		Tableau t = new Tableau(found1, new Card[4], tableau1);
		TableauMoveIterator tmi = new TableauMoveIterator(t, 10);
		assertNotNull(tmi);
		assertTrue(tmi.hasNext());
	}

	@Test
	public final void testNext() {
		Tableau t = new Tableau(found1, new Card[Tableau.FREECELL_COUNT], tableau1);
		MoveTree m = new MoveTree();
		TableauMoveIterator tmi = new TableauMoveIterator(t, m, 120, 0);
		assertNotNull(tmi);
		assertTrue(tmi.hasNext());
		MoveTree next = tmi.next();
		assertNotNull(next);
		assertNotNull(next.parent());
		assertNull(next.parent().parent());
	}

	@Test
	public final void testGetNext() {
		Tableau t = new Tableau(found1, new Card[Tableau.FREECELL_COUNT], tableau1);
		TableauMoveIterator tmi = new TableauMoveIterator(t, 2);
		assertNotNull(tmi);
		assertTrue(tmi.hasNext());
		MoveTree m1 = tmi.next();
		assertNotNull(m1);
		assertTrue(tmi.hasNext());
		MoveTree m2 = tmi.next();
		assertNotNull(m2);
		int count = 0;
		while (tmi.hasNext()) {
			MoveTree mx = tmi.next();
			count += 1;
		}
		assertEquals(121, count);
	}

	@Test
	public final void testNextWide() {
		Tableau t = new Tableau(found1, new Card[Tableau.FREECELL_COUNT], tableau1);
		TableauMoveIterator tmi = new TableauMoveIterator(t, 2);
		assertNotNull(tmi);
		assertTrue(tmi.hasNext());
		MoveTree m1 = tmi.next();
		assertNotNull(m1);
		assertTrue(tmi.hasNext());
		MoveTree m2 = tmi.nextWide();
		assertNotNull(m2);
	}
}
