package explore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import freecellState.Location;
import freecellState.Location.Area;
import freecellState.Move;
import freecellState.Mover;
import freecellState.Tableau;

public class MoveCalculatorTest {
	private static final String SHORTDECK = "AS,7S,6H,5C,4H,3C,2D,KD"; // AS,2D,3C,4H,5C,6H,7S,KD
	private static final String DECKSTRING = "3H,KH,2D,KD,JD,4H,9D,TS,7C,KS,7D,QH,8C,6H,4C,9S,7H,6C,2C,2H,5D,3D,8S,JH,TC,AD,7S,QS,8D,9H,5C,6S,5S,AH,TH,KC,3C,4D,9C,AS,4S,QC,JC,AC,3S,TD,QD,8H,5H,6D,JS,2S";
	private static final Location[] Found = {
			new Location(Area.Foundation, 0, 0, -1),
			new Location(Area.Foundation, 1, 0, -1),
			new Location(Area.Foundation, 2, 0, -1),
			new Location(Area.Foundation, 3, 0, -1),
	};
	
	private static final Location[] Tab = {
			new Location(Area.Tableau, 0, 0, 0),
			new Location(Area.Tableau, 1, 0, 1),
			new Location(Area.Tableau, 2, 0, 2),
			new Location(Area.Tableau, 3, 0, 3),
			new Location(Area.Tableau, 4, 0, 4),
			new Location(Area.Tableau, 5, 0, 5),
			new Location(Area.Tableau, 6, 0, 6),
			new Location(Area.Tableau, 7, 0, 7),
	};
	
	private static final Location[] Free = {
			new Location(Area.Freecell, 0, 0, -1),
			new Location(Area.Freecell, 1, 0, -1),
			new Location(Area.Freecell, 2, 0, -1),
			new Location(Area.Freecell, 3, 0, -1),
	};
	
	private static final Move[] shortMoves = {
			new Move(Tab[0], Found[3]),
			new Move(Tab[1], Tab[2]),
			new Move(Tab[2], Tab[3]),
			new Move(Tab[3], Tab[4]),
			new Move(Tab[4], Tab[5]),
			new Move(Tab[5], Tab[6]),
			new Move(Tab[1], Free[0]),
			new Move(Tab[2], Free[0]),
			new Move(Tab[3], Free[0]),
			new Move(Tab[4], Free[0]),
			new Move(Tab[5], Free[0]),
			new Move(Tab[6], Free[0]),
			new Move(Tab[7], Free[0])
	};
	
	private static final Move[] tableau1Moves = {
			new Move(Tab[3], Tab[7]),
			new Move(Tab[6], Tab[3]),
			new Move(Tab[0], Free[0]),
			new Move(Tab[1], Free[0]),
			new Move(Tab[2], Free[0]),
			new Move(Tab[3], Free[0]),
			new Move(Tab[4], Free[0]),
			new Move(Tab[5], Free[0]),
			new Move(Tab[6], Free[0]),
			new Move(Tab[7], Free[0])
	};
	
	private static final Move[] tableau1Moves2 = {
			new Move(Tab[6], Tab[3]),
			new Move(Tab[6], Tab[7]),
			new Move(Tab[0], Free[0]),
			new Move(Tab[1], Free[0]),
			new Move(Tab[2], Free[0]),
			new Move(Tab[3], Free[0]),
			new Move(Tab[4], Free[0]),
			new Move(Tab[5], Free[0]),
			new Move(Tab[6], Free[0]),
			new Move(Tab[7], Free[0])
	};
	
	private Tableau shortTab;
	private Tableau tableau1;
	
	@Before
	public void setUp() throws Exception {
		shortTab = Tableau.fromStringNoValidation(SHORTDECK);
		tableau1 = Tableau.fromStringNoValidation(DECKSTRING);
		Move.debuggingMove = false;
	}

	@Test
	public final void testShort() {
		System.out.println("shortTab = " + shortTab);
		Move[] moves = MoveCalculator.movesFrom(shortTab, false);
		System.out.println("moves = " + Arrays.toString(moves));
		assertNotNull(moves);
		ArrayIterator<Move> mi = new ArrayIterator<Move>(moves);
		assertTrue(mi.hasNext());
		int shortCount = 0;
		while (mi.hasNext()) {
			Move m = mi.next();
			System.out.println("move(" + shortCount + "): " + m);
			assertEquals(shortMoves[shortCount++], m);
		}
		assertFalse(mi.hasNext());
		System.out.println("---------------");
	}

	@Test
	public final void testDeck1Moves() {
		System.out.println("tableau1 =\n" + tableau1);
		Move[] moves = MoveCalculator.movesFrom(tableau1, false);
		System.out.println("moves = " + Arrays.toString(moves));
		assertNotNull(moves);
		ArrayIterator<Move> mi = new ArrayIterator<Move>(moves);
		assertTrue(mi.hasNext());
		HashSet<Move> moveSet = new HashSet<Move>(tableau1Moves.length);
		for (Move m : tableau1Moves) {
			moveSet.add(m);
		}
		while (mi.hasNext()) {
			Move m = mi.next();
			System.out.println("move: " + m);
			assertTrue(moveSet.contains(m));
			assertTrue(moveSet.remove(m));
		}
		assertFalse(mi.hasNext());
		assertTrue(moveSet.isEmpty());
		System.out.println("---------------");
		assertNotNull(moves[0]);
		Move firstMove = moves[0];
		Tableau nt = null;
		try {
			nt = Mover.move(tableau1, firstMove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Move[] moves2 = MoveCalculator.movesFrom(nt, false);
		mi = new ArrayIterator<Move>(moves2);
		for (Move m: tableau1Moves2) {
			moveSet.add(m);
		}
		while (mi.hasNext()) {
			Move m = mi.next();
			System.out.println(m);
			assertTrue(moveSet.contains(m));
			assertTrue(moveSet.remove(m));
		}
		assertFalse(mi.hasNext());
		assertTrue(moveSet.isEmpty());
	}
}
