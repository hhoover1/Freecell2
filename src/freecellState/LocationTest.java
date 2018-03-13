package freecellState;

import static org.junit.Assert.*;

import org.junit.Test;

import freecellState.Location.Area;

public class LocationTest {
	Location loc1 = new Location(Area.Foundation, 1);
	Location loc2 = new Location(Area.Freecell, 3);
	Location loc3 = new Location(Area.Tableau, 5, 1, 3);
	
	@Test
	public final void testHashCode() {
		int hash = loc1.hashCode();
		assertEquals(8300522, hash);
	}

	@Test
	public final void testArea() {
		assertEquals(Area.Foundation, loc1.area());
		assertEquals(Area.Freecell, loc2.area());
		assertEquals(Area.Tableau, loc3.area());
	}

	@Test
	public final void testColumn() {
		assertEquals(1, loc1.column());
		assertEquals(3, loc2.column());
		assertEquals(5, loc3.column());
	}

	@Test
	public final void testOffset() {
		assertEquals(0, loc1.offset());
		assertEquals(0, loc2.offset());
		assertEquals(1, loc3.offset());
	}

	@Test
	public final void testOriginalColumn() {
		assertEquals(0, loc1.originalColumn());
		assertEquals(0, loc2.originalColumn());
		assertEquals(3, loc3.originalColumn());
	}

	@Test
	public final void testEqualsObject() {
		assertFalse(loc1.equals(loc2));
		assertFalse(loc2.equals(loc3));
		assertFalse(loc3.equals(loc1));
		assertFalse(loc2.equals(loc1));
		assertFalse(loc3.equals(loc2));
		assertFalse(loc1.equals(loc3));
		Location tl1 = new Location(Area.Foundation, 1);
		Location tl2 = new Location(Area.Foundation, 2);
		assertTrue(tl1.equals(loc1));
		assertFalse(tl1.equals(loc2));
		assertTrue(loc1.equals(tl1));
		assertFalse(tl1.equals(tl2));
		assertFalse(tl2.equals(tl1));
	}

	@Test
	public final void testToString() {
		assertEquals("Location(Foundation(0), 1, 0)", loc1.toString());
	}

	@Test
	public final void testDebugShortName() {
		assertEquals("Fo10", loc1.debugShortName());
	}

	@Test
	public final void testShortName() {
		assertEquals("Fo10", loc1.shortName());
	}
}
