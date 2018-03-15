package freecellState;

import static org.junit.Assert.*;

import org.junit.Test;

import deck.Card;
import freecellState.Location.Area;

public class MoveTest {
	private final Move[] moves1 = {
			new Move(new Location(Area.Tableau, 2, 0, 2), new Location(Area.Foundation, 3)),
			new Move(new Location(Area.Tableau, 6, 0, 6), new Location(Area.Foundation, 0)),
			new Move(new Location(Area.Freecell, 0), new Location(Area.Tableau, 1, 0, 1)),
			new Move(new Location(Area.Tableau, 2, 0, 2), new Location(Area.Foundation, 3)),
			new Move(new Location(Area.Tableau, 6, 0, 6), new Location(Area.Foundation, 0)),
			new Move(new Location(Area.Freecell, 0), new Location(Area.Tableau, 1, 0, 1))
	};
	
	@Test
	public final void testHashCode() {
		int h1 = moves1[0].hashCode();
		int h2 = moves1[1].hashCode();
		int h3 = moves1[2].hashCode();
		int g1 = moves1[3].hashCode();
		int g2 = moves1[4].hashCode();
		int g3 = moves1[5].hashCode();
		assertEquals(h1, g1);
		assertNotEquals(h1, h2);
		assertEquals(h2, g2);
		assertNotEquals(h2, h3);
		assertEquals(h3, g3);
		assertNotEquals(h3, h1);
	}

	@Test
	public final void testCard() {
		assertTrue(moves1[0].card() == null);
	}

	@Test
	public final void testFrom() {
		assertEquals(new Location(Area.Tableau, 2, 0, 4), moves1[0].from());
	}

	@Test
	public final void testTo() {
		assertEquals(new Location(Area.Foundation, 3), moves1[0].to());
	}

	@Test
	public final void testSetCard() {
		moves1[0].setCard(Card.cardFrom("AS"));
		assertEquals(Card.cardFrom("AS"), moves1[0].card());
	}

	@Test
	public final void testShortName() {
		String s1 = moves1[0].shortName();
		assertEquals("Move(Ta20:Fo30)", s1);
	}

	@Test
	public final void testToString() {
		String s1 = moves1[0].toString();
		assertEquals("Move(Ta20:Fo30)", s1);
	}

	@Test
	public final void testCompareTo() {
		int cmp = moves1[0].compareTo(moves1[1]);
		assertTrue (cmp < 0);
	}

	@Test
	public final void testEqualsObject() {
		assertEquals(moves1[0], moves1[3]);
		assertNotSame(moves1[0], moves1[3]);
	}

}
