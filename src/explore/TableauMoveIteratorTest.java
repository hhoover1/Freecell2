package explore;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import deck.Card;
import freecellState.Tableau;
import freecellState.TableauStack;

public class TableauMoveIteratorTest {
//	private static final Location Fo00 = new Location(Area.Foundation, 0, 0);
//	private static final Location Ta40 = new Location(Area.Tableau, 4, 0);
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
	public final void testTableauMoveIteratorTableauInt() throws Exception {
		Tableau t = new Tableau(found1, new Card[4], TableauStack.fromCardArray(tableau1), false);
		System.out.println(t);
		MoveTree root = new MoveTree();
		TableauMoveIterator tmi = new TableauMoveIterator(t, root, 10, 0);
		assertNotNull(tmi);
	}

	@Test
	public final void testTableauMoveIteratorTableauMoveTreeInt() throws Exception {
		Tableau t = new Tableau(found1, new Card[Tableau.FREECELL_COUNT], TableauStack.fromCardArray(tableau1), false);
		MoveTree m = new MoveTree();
		TableauMoveIterator tmi = new TableauMoveIterator(t, m, 120, 0);
		assertNotNull(tmi);
	}

//	private void printTableauAndHash(Tableau t, MoveTree m1) {
//		Tableau st = m1.resultingTableau(t);
//		System.out.println(st);
//		System.out.println(st.tableauHash());
//	}
}
