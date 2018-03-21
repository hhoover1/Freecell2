package explore;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import freecellState.TableauHash;

public class ExaminedStatesMapTest {
	static final byte[] hashBits = {
			1,2,3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9
	};
	
	static final byte[] hashBits2 = {
			1,2,3,4,5,6,7,8,9,10,11,13,
			1,2,3,4,5,6,7,8,9,10,11,14,
			1,2,3,4,5,6,7,8,9,10,11,15,
			1,2,3,4,5,6,7,8,9
	};
	
	static final byte[] hashBits3 = {
			2,3,4,5,6,7,8,9,10,11,13,1,
			2,3,4,5,6,7,8,9,10,11,14,1,
			1,2,3,4,5,6,7,8,9,10,11,15,
			1,2,3,4,5,6,7,8,9
	};
	
	static final byte[] hashBits4 = {
			2,3,4,5,6,7,8,9,10,11,12,13,
			1,2,3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8
	};
	
	static final byte[] hashBits5 = {
			1,2,3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9,10,11,12,
			3,4,5,6,7,8,9,10,11,12,13,2,
			1,2,3,4,5,6,7,8,9
	};
	
	static final byte[] hashBits6 = {
			3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9,10,11
	};
	
	static final byte[] hashBits7 = {
			3,1,5,6,7,8,9,10,11,12,
			1,2,3,4,5,6,7,8,9,10,11
	};
	
	ExaminedStatesMap theMap = null;
	static final TableauHash hash = new TableauHash(hashBits);
	static final TableauHash hash2 = new TableauHash(hashBits);
	static final TableauHash hash3 = new TableauHash(hashBits2);
	static final TableauHash hash4 = new TableauHash(hashBits3);
	static final TableauHash hash5 = new TableauHash(hashBits4);
	static final TableauHash hash6 = new TableauHash(hashBits5);
	static final TableauHash hash7 = new TableauHash(hashBits6);
	static final TableauHash hash8 = new TableauHash(hashBits7);
	
	@Before
	public void setUp() throws Exception {
		theMap = new ExaminedStatesMap(1024);
	}

	@Test
	public final void testExaminedStatesMap() {
		ExaminedStatesMap esm = new ExaminedStatesMap(32);
		assertNotNull(esm);
		assertNotNull(theMap);
	}

	@Test
	public final void testSize() {
		assertEquals(0, theMap.size());
		theMap.put(hash, new Integer(2));
		assertEquals(1, theMap.size());
		theMap.put(hash,  new Integer(3));
		assertEquals(1, theMap.size());
	}

	@Test
	public final void testIsEmpty() {
		assertNotNull(theMap);
		assertTrue(theMap.isEmpty());
		mapFill(false);
		assertFalse(theMap.isEmpty());
	}

	@Test
	public final void testContainsKey() {
		assertFalse(theMap.containsKey(hash));
		assertFalse(theMap.containsKey(hash2));
		assertFalse(theMap.containsKey(hash3));
		mapFill(false);
		assertTrue(theMap.containsKey(hash));
		assertTrue(theMap.containsKey(hash2));
		assertTrue(theMap.containsKey(hash3));
		assertFalse(theMap.containsKey(hash8));
	}

	@Test
	public final void testGet() {
		mapFill(false);
		assertEquals(new Integer(7), theMap.get(hash));
		assertEquals(new Integer(1), theMap.get(hash3));
		assertNull(theMap.get(hash8));
	}

	@Test
	public final void testPut() {
		assertNotNull(theMap);
		assertEquals(0, theMap.size());
		mapFill(false);
		assertNotNull(theMap.put(hash2, new Integer(8)));
		assertEquals(2, theMap.size());
	}

	/**
	 * 
	 */
	private void mapFill(boolean big) {
		assertNull(theMap.put(hash, new Integer(7)));
		assertEquals(1, theMap.size());
		assertNull(theMap.put(hash3, new Integer(1)));
		assertEquals(2, theMap.size());
		if (big) {
			theMap.put(hash4, new Integer(100));
			theMap.put(hash5, new Integer(3));
			theMap.put(hash6, new Integer(4));
			theMap.put(hash7, new Integer(5));
		}
	}

	@Test
	public final void testRemove() {
		mapFill(false);
		assertEquals(2, theMap.size());
		Integer val = theMap.remove(hash);
		assertEquals(1, theMap.size());
		assertNotNull(val);
		assertEquals(new Integer(7), val);
	}

	@Test
	public final void testClear() {
		mapFill(false);
		assertEquals(2, theMap.size());
		theMap.clear();
		assertEquals(0, theMap.size());
	}

	@Test
	public final void testCompactExaminedStates() {
		mapFill(true);
		theMap.compactExaminedStates(50);
		assertFalse(theMap.isEmpty());
		assertEquals(6, theMap.size());
		assertTrue(theMap.containsKey(hash));
		assertTrue(theMap.containsKey(hash6));
		assertFalse(theMap.containsKey(hash8));
	}

}
