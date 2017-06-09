package explore;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import freecellState.Location;
import freecellState.Location.Area;
import freecellState.Move;

public class MoveTreeTest {
	private final MoveTree nullParent = null;
	private final Location free0 = new Location(Area.Freecell, 0, 0);
	private final Location free1 = new Location(Area.Freecell, 1, 0);
	private final Location tab00 = new Location(Area.Tableau, 0, 0);
	private final Move move1 = new Move(free0, free1);
	private final Move move2 = new Move(free1, free0);
	private final Move move3 = new Move(free0, tab00);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testMoveTree() {
		MoveTree mt = new MoveTree(nullParent, move1, 1000);
		assertNotNull(mt);
	}

	@Test
	public final void testAddChild() {
		MoveTree parent = new MoveTree(nullParent, move1, 1000);
		MoveTree child1 = new MoveTree(parent, move2, 999);
		assertNotNull(child1);
		assertTrue(parent.hasChildren());
	}

	@Test
	public final void testMove() {
		MoveTree parent = new MoveTree(nullParent, move1, 0);
		assertEquals(move1, parent.move());
	}

	@Test
	public final void testHasChildren() {
		MoveTree parent = new MoveTree(nullParent, move1, 1000);
		assertFalse(parent.hasChildren());
		@SuppressWarnings("unused")
		MoveTree child1 = new MoveTree(parent, move2, 1003);
		assertTrue(parent.hasChildren());
	}
	
	@Test
	public final void testToString() {
		MoveTree parent = new MoveTree(nullParent, move1, 1000);
		assertFalse(parent.hasChildren());
		MoveTree child1 = new MoveTree(parent, move2, 1003);
		String sn = child1.toString();
		//System.out.println(sn);
		assertEquals("MoveTree(^0,Move(Loc(Fr10):Loc(Fr00)),1,1003)", sn);
		sn = parent.toString();
		//System.out.println(sn);
		assertEquals("MoveTree(1,Move(Loc(Fr00):Loc(Fr10)),0,1000)", sn);
	}

	@Test
	public final void testChildIterator() {
		MoveTree[] trees = new MoveTree[5];
		MoveTree parent = new MoveTree(nullParent, move1, 1001);
		MoveTree child1 = new MoveTree(parent, move2, 1002);
		trees[0] = parent;
		trees[1] = child1;
		assertNotNull(child1);
		assertTrue(parent.hasChildren());
		assertNotNull(parent.childIterator());
		assertTrue(parent.childIterator().hasNext());
		MoveTree child = parent.childIterator().next();
		assertNotNull(child);
		assertEquals(child1, child);
		MoveTree child2 = new MoveTree(child1, move3, 1004);
		trees[2] = child2;
		MoveTree child3 = new MoveTree(child1, move3, 1000);
		trees[3] = child3;
		MoveTree child4 = new MoveTree(parent, move3, 992);
		trees[4] = child4;
		assertTrue(parent.hasChildren());
		assertNotNull(parent.childIterator());
		assertTrue(parent.childIterator().hasNext());
		Iterator<MoveTree> kids = parent.iterator();
		int count = 0;
		while (kids.hasNext()) {
			MoveTree n = kids.next();
			assertEquals(trees[count], n);
			count += 1;
		}
		assertEquals(5, count);
	}

	@Test
	public final void testMTIterator() {
		MoveTree parent = new MoveTree(nullParent, move1, 1000);
		MoveTree[] childs = makeChilds(parent);

		Iterator<MoveTree> iter = parent.iterator();
		assertTrue(iter.hasNext());
		assertEquals(parent, iter.next());
		int idx = 0;
		while (iter.hasNext()) {
			assertEquals(childs[idx++], iter.next());
		}
		assertEquals(8, idx);
	}
	
	@Test
	public final void testMoves() {
		MoveTree parent = new MoveTree(nullParent, move1, 999);
		MoveTree[] childs = makeChilds(parent);
		Move[] c111 = childs[2].moves();
		assertNotNull(c111);
		assertEquals(4, c111.length);
		assertEquals(parent.move(), c111[0]);
		assertEquals(childs[2].move(), c111[3]);
		Move[] c31 = childs[7].moves();
		assertNotNull(c31);
		assertEquals(3, c31.length);
		assertEquals(parent.move(), c31[0]);
		assertEquals(childs[6].move(), c31[1]);
		assertEquals(childs[7].move(), c31[2]);
	}

	private MoveTree[] makeChilds(MoveTree parent) {
		MoveTree child1 = new MoveTree(parent, move2, 998);
		MoveTree child11 = new MoveTree(child1, move3, 997);
		MoveTree child111 = new MoveTree(child11, move1, 996);
		MoveTree child112 = new MoveTree(child11, move2, 1001);
		MoveTree child113 = new MoveTree(child11, move3, 1002);
		MoveTree child2 = new MoveTree(parent, move1, 995);
		MoveTree child3 = new MoveTree(parent, move3, 1003);
		MoveTree child31 = new MoveTree(child3, move1, 994);
		MoveTree childs[] = new MoveTree[] { child1, child11, child111, child112, child113, child2, child3, child31 };
		return childs;
	}
}
