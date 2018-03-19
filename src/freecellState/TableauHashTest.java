package freecellState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import control.StagedDepthFirstSolver;

public class TableauHashTest {
	private static final byte[] t1Bytes = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
	private static final byte[] t2Bytes = { 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00 };
	
	TableauHash t1;
	TableauHash t2;
	TableauHash t3;
	
	@Before
	public void setup() {
		t1 = new TableauHash(t1Bytes);
		t2 = new TableauHash(t2Bytes);
		t3 = new TableauHash(t1Bytes);
	}
	
	@Test
	public final void testHashCode() {
		int hash = t1.hashCode();
		assertEquals(hash, 67372036);
	}

	@Test
	public final void testTableauHashTableau() {
		Tableau tableau = Tableau.fromString(StagedDepthFirstSolver.DECKSTRING_38);
		assertNotNull(tableau);
		System.out.println("tableau = " + tableau);
		TableauHash lt1 = new TableauHash(tableau);
		assertNotNull(lt1);
		System.out.println("hash = " + lt1);
	}

	@Test
	public final void testEqualsObject() {
		assertNotEquals(t1, "this is not a TableauHash");
		assertNotEquals(t1, t2);
		assertEquals(t1, t3);
	}

	@Test
	public final void testCompareTo() {
		assertEquals(0, t1.compareTo(t3));
		assertEquals(0, t3.compareTo(t1));
		assertEquals(-1, t1.compareTo(t2));
		assertEquals(1, t2.compareTo(t1));
	}

	@Test
	public final void testEqualsObject1() {
		assertTrue(t1.equals(t3));
		assertTrue(t3.equals(t1));
		assertFalse(t1.equals(t2));
		assertFalse(t2.equals(t1));
	}

	@Test
	public final void testToString() {
		String ts1 = t1.toString();
		assertNotNull(ts1);
		assertEquals(ts1, "TableauHash(67372036, 0010203040506070)");
	}
}
