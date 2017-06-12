package explore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import deck.Card;
import freecellState.Location;
import freecellState.Location.Area;
import freecellState.Move;
import freecellState.Tableau;

public class TableauMoveIteratorTest {
	private static final Location Fo00 = new Location(Area.Foundation, 0, 0);
	private static final Location Ta40 = new Location(Area.Tableau, 4, 0);
	@SuppressWarnings("unused")
	private static String testDeck1 = "KH,KS,KC,KD";
	private static Card[] found1 = new Card[] { Card.cardFrom("QH"), Card.cardFrom("QC"), Card.cardFrom("QD"),
			Card.cardFrom("QS") };
	private static Card[][] tableau1 = { { Card.cardFrom("KC") }, { Card.cardFrom("KD") }, { Card.cardFrom("KH") },
			{ Card.cardFrom("KS") }, {}, {}, {}, {} };

	@Before
	public void setUp() throws Exception {
		TableauMoveIterator.clearExamined();
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
	public final void testGetNext() throws Exception {
		System.out.println("+++++++++++++++++++");
		Tableau t = new Tableau(found1, new Card[Tableau.FREECELL_COUNT], tableau1);
		TableauMoveIterator tmi = new TableauMoveIterator(t, 2);
		System.out.println(t);
		assertNotNull(tmi);
		assertTrue(tmi.hasNext());
		ArrayIterator<Move> mvs1 = tmi.moves();
		assertTrue(mvs1.hasNext());
		Move[] mvArray = tmi.moveArray();
		assertNotNull(mvArray);
		assertNotNull(mvArray[0]);
		assertEquals(new Move(Ta40, Fo00), mvArray[0]);
		MoveTree m1 = tmi.next();
		assertNotNull(m1);
		assertEquals(1, m1.depth());
		printTableauAndHash(t, m1);
		assertTrue(tmi.hasNext());
		MoveTree m2 = tmi.next();
		assertNotNull(m2);
		assertEquals(2, m2.depth());
		printTableauAndHash(t, m2);
		int count = 2;
		while (tmi.hasNext()) {
			MoveTree mx = tmi.next();
			assertNotNull(mx);
			printTableauAndHash(t, mx);
			System.out.println(String.format("%4d - %s\n", count, mx));
			count += 1;
		}
		assertEquals(27, count);
		System.out.println("+++++++++++++++++++");
	}

	private void printTableauAndHash(Tableau t, MoveTree m1) {
		Tableau st = m1.resultingTableau(t);
		System.out.println(st);
		System.out.println(st.tableauHash());
	}

	@Test
	public final void testNextWide() {
		System.out.println("----------testNextWide-------------");
		Tableau t = new Tableau(found1, new Card[Tableau.FREECELL_COUNT], tableau1);
		TableauMoveIterator tmi = new TableauMoveIterator(t, 2);
		assertNotNull(tmi);
		assertTrue(tmi.hasNext());
		MoveTree m1 = tmi.nextWide();
		assertNotNull(m1);
		assertEquals(1, m1.depth());
		assertTrue(tmi.hasNext());
		MoveTree m2 = tmi.nextWide();
		assertNotNull(m2);
		assertEquals(1, m2.depth());
		TableauMoveIterator tmi2 = new TableauMoveIterator(t, 3);
		int count = 0;
		while (tmi2.hasNext()) {
			MoveTree mx = tmi2.nextWide();
			assertNotNull(mx);
			count += 1;
		}
		assertEquals(6, count);
		MoveTree root = tmi2.treeRoot();
		count = 0;
		Iterator<MoveTree> iter = root.iterator();
		while (iter.hasNext()) {
			MoveTree mx = iter.next();
			assertNotNull(mx);
			System.out.println(mx);
			count += 1;
		}
		assertEquals(7, count);
	}
}
