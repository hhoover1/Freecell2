/**
 * 
 */
package explore;

import java.util.ArrayList;
import java.util.List;

import deck.Card;
import freecellState.Location;
import freecellState.Location.Area;
import freecellState.Move;
import freecellState.Tableau;

/**
 * @author hhoover
 *
 */
public class MoveCalculator {
	public static Move[] movesFrom(Tableau t, boolean foundationOnly) {
		List<Move> lm = calculatePossibleMoves(t, null, foundationOnly);
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
}
