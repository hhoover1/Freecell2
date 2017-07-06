/**
 * 
 */
package explore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import deck.Card;
import deck.Card.Suit;
import freecellState.Location;
import freecellState.Location.Area;
import freecellState.Move;
import freecellState.Tableau;

/**
 * @author hhoover
 *
 */
public class MoveCalculator {
	private static MoveCalculator mc = new MoveCalculator();

	public static Move[] movesFrom(Tableau t, boolean foundationOnly) {
		List<Move> lm = calculatePossibleMoves(t, null, foundationOnly);
		if (!foundationOnly) {
			lm.sort(mc.new MoveValueComparator(t));
		}
		Move[] res = lm.toArray(new Move[0]);
		return res;
	}

	public static List<Move> calculatePossibleMoves(Tableau tableau, Move lastMove, boolean foundationOnly) {
		List<Move> possibleMoveCollection = new ArrayList<Move>();

		// check if any freecells can land in the foundation
		List<Move> free2found = checkFreecellsToFoundation(tableau);
		possibleMoveCollection.addAll(free2found);

		List<Move> tabToFound = checkTableauToFoundation(tableau);
		possibleMoveCollection.addAll(tabToFound);

		if (!foundationOnly) {
			// check if any freecells can land on a tableau column
			List<Move> freeToTab = checkFreecellsToTableau(tableau);
			possibleMoveCollection.addAll(freeToTab);

			// locate moveable stacks in tableau
			// check each step of the column to see if any can land. largest
			// first.
			List<Location> moveableColumns = moveableColumnsOfTableau(tableau, lastMove);
			List<Move> viableTableauMoves = legalMovesFrom(tableau, moveableColumns);
			possibleMoveCollection.addAll(viableTableauMoves);

			// propose each of the top tableau cards to the first open freecell
			List<Move> freecellMoves = freecellMoves(tableau, lastMove);
			possibleMoveCollection.addAll(freecellMoves);
		}

		return possibleMoveCollection;
	}

	static List<Move> checkTableauToFoundation(Tableau tableau) {
		List<Move> result = new ArrayList<Move>();
		for (int tabCol = 0; tabCol < Tableau.TABLEAU_SIZE; ++tabCol) {
			Card c = tableau.getTopTableau(tabCol);
			if (c != null) {
				Location to = new Location(Area.Foundation, c.suit().ordinal(), 0);
				Card oc = tableau.foundation(to.column());
				if ((oc == null && c.rank() == 1) || (oc != null && c.isNextRankOf(oc))) {
					Location from = new Location(Area.Tableau, tabCol, 0);
					Move m = new Move(from, to);
					result.add(m);
				}
			}
		}

		return result;
	}

	static List<Move> checkFreecellsToFoundation(Tableau tableau) {
		List<Move> result = new ArrayList<Move>();
		for (int freeIdx = 0; freeIdx < Tableau.FREECELL_COUNT; ++freeIdx) {
			Card frc = tableau.freecell(freeIdx);
			if (frc != null) {
				int foundIdx = frc.suit().ordinal();
				Card foc = tableau.foundation(foundIdx);
				if ((foc != null && frc.isNextRankOf(foc)) || ((foc == null && frc.rank() == 1))) {
					Location from = new Location(Area.Freecell, freeIdx, 0);
					Location to = new Location(Area.Foundation, foundIdx, 0);
					Move m = new Move(from, to);
					result.add(m);
				}
			}
		}

		return result;
	}

	static List<Move> checkFreecellsToTableau(Tableau tableau) {
		List<Move> result = new ArrayList<Move>();
		for (int freeIdx = 0; freeIdx < Tableau.FREECELL_COUNT; ++freeIdx) {
			Card frc = tableau.freecell(freeIdx);
			if (frc != null && frc.rank() != 1) {
				for (int tabIdx = 0; tabIdx < Tableau.TABLEAU_SIZE; ++tabIdx) {
					Card ftc = tableau.getTopTableau(tabIdx);
					if (ftc == null || frc.canBePlacedOn(ftc)) {
						Location from = new Location(Area.Freecell, freeIdx, 0);
						Location to = new Location(Area.Tableau, tabIdx, 0);
						Move m = new Move(from, to);
						result.add(m);
					}
				}
			}
		}

		return result;
	}

	static List<Move> legalMovesFrom(Tableau tableau, List<Location> moveableColumns) {
		List<Move> moves = new ArrayList<Move>();
		for (Location l : moveableColumns) {
			List<Card> pstack = tableau.getCards(l);
			if (pstack.isEmpty()) {
				continue;
			}

			Card bottomCard = pstack.get(0);
			if (bottomCard.rank() == 1) {
				// skip aces here - only move to foundation...
				continue;
			}
			for (int tabCol = 0; tabCol < Tableau.TABLEAU_SIZE; ++tabCol) {
				if (tabCol == l.column()) {
					continue;
				}

				if (bottomCard.canBePlacedOn(tableau.getTopTableau(tabCol))) {
					Location to = new Location(Area.Tableau, tabCol, 0);
					Move m = new Move(l, to);
					moves.add(m);
				}
			}
		}

		return moves;
	}

	static List<Location> moveableColumnsOfTableau(Tableau tableau, Move lastMove) {
		List<Location> moves = new ArrayList<Location>();

		for (int tabCol = 0; tabCol < Tableau.TABLEAU_SIZE; ++tabCol) {
			// skip any card we JUST placed
			if (lastMove != null && lastMove.to().area() == Area.Tableau && tabCol == lastMove.to().column()) {
				continue;
			}

			Location l = new Location(Area.Tableau, tabCol, 0);
			moves.add(l);
			/*
			 * skip moving more than one card at a time for now. List<Card>
			 * colCards = tableau.getColumn(tabCol); if (colCards.isEmpty()) {
			 * continue; }
			 * 
			 * Card aboveCard = colCards.get(colCards.size() - 1); for (int
			 * offset = 1; offset < colCards.size(); ++offset) { Card belowCard
			 * = colCards.get(colCards.size() - 1 - offset); if
			 * (aboveCard.canBePlacedOn(belowCard)) { l = new
			 * Location(Area.Tableau, tabCol, offset); moves.add(l); } else {
			 * break; }
			 * 
			 * aboveCard = belowCard; }
			 */
		}

		return moves;
	}

	static List<Move> freecellMoves(Tableau tableau, Move lastMove) {
		int lastCol = -1;
		if (lastMove != null && lastMove.to().area() == Area.Tableau) {
			lastCol = lastMove.to().column();
		}

		ArrayList<Move> moves = new ArrayList<Move>();
		int openFree = tableau.firstEmptyFreecell();
		if (openFree >= 0) {
			Location to = new Location(Area.Freecell, openFree, 0);

			for (int tabCol = 0; tabCol < Tableau.TABLEAU_SIZE; ++tabCol) {
				if (tabCol == lastCol) {
					continue;
				}

				if (tableau.getTopTableau(tabCol) != null) {
					Location from = new Location(Area.Tableau, tabCol, 0);
					Move m = new Move(from, to);
					moves.add(m);
				}
			}
		}

		return moves;
	}

	private static int depthOfCard(Tableau t, int column, Card c) {
		for (int depth = 0; depth < t.heightOfTableauStack(column); ++depth) {
			Card test = t.getTableau(column, depth);
			if (test != null && test.isNextRankOf(c)) {
				return depth;
			}
		}

		return -1;
	}

	public static int releaseToFoundationDepth(Tableau tableau, int column) {
		int shallowest = Integer.MAX_VALUE;
		for (Suit s : Suit.values()) {
			Card top = tableau.foundation(s.ordinal());
			if (top == null) {
				top = new Card(s, 1);
			}
			int d = depthOfCard(tableau, column, top);
			if (d >= 0) {
				shallowest = Math.min(shallowest, d);
			}
		}

		if (shallowest < Integer.MAX_VALUE) {
			return shallowest;
		}

		return -1;
	}

	public static int releaseToTableauDepth(Tableau tableau, int column) {
		Card[] tops = tableau.topTableauCards();

		int shallowest = Integer.MAX_VALUE;
		for (int ii = 0; ii < Tableau.TABLEAU_SIZE; ++ii) {
			if (ii == column) {
				continue;
			}

			Card top = tops[ii];
			if (top != null) {
				int d = depthOfCard(tableau, column, top);
				if (d >= 0) {
					shallowest = Math.min(shallowest, d);
				}
			}
		}

		if (shallowest < Integer.MAX_VALUE) {
			return shallowest;
		}

		return -1;
	}

	public class MoveValueComparator implements Comparator<Move> {
		private Tableau _tableau;

		public MoveValueComparator(Tableau t) {
			_tableau = t;
		}

		// ordering of moves, valuable to less:
		// to foundation
		// to tableau, releasing a to-foundation move
		// to freecell, releasing a to-foundation move
		// tableau to tableau, releasing a sequence of freecell-to-tableau moves
		// tableau to tableau, releasing a to-tableau move
		// tableau to tableau, releasing a freecell-to-tableau move
		// tableau to freecell, releasing a sequence of freecell-to-tableau
		// moves
		// tableau to freecell, releasing a freecell to tableau move
		// all of the above, transitively from two below.
		// to tableau, no release
		// to freecell, no release

		private int moveValue(Move m) {
			int result = 0;

			switch (m.to().area()) {
			case Foundation:
				result = 1000;
				break;

			case Tableau:
				int rtfd = MoveCalculator.releaseToFoundationDepth(_tableau, m.from().column());
				if (rtfd >= 0) {
					result = 100 - (10 * rtfd);
					break;
				}

				int rttd = MoveCalculator.releaseToTableauDepth(_tableau, m.from().column());
				if (rttd >= 0) {
					result = 70 - (7 * rttd);
				}

				break;
			case Freecell:
				rtfd = MoveCalculator.releaseToFoundationDepth(_tableau, m.from().column());
				if (rtfd >= 0) {
					result = 10 - rtfd;
					break;
				}

				break;
			}

			return result;
		}

		@Override
		public int compare(Move o1, Move o2) {
			return moveValue(o2) - moveValue(o1);
		}
	}
}
