package freecellState;

import java.util.Arrays;

import deck.Card;
import explore.MoveTree;
import freecellState.Location.Area;

public class Mover {
	public static Tableau move(Tableau tableau, Move m) throws Exception {
		Location from = m.from();
		Location to = m.to();
		int moveCount = from.offset();
		if (from.area() == Location.Area.Foundation) {
			throw new Exception("cannot move FROM foundation");
		} else if (to.area() == Area.Freecell && tableau._freecells[to.column()] != null) {
			throw new Exception("move to non-empty freecell!");
		} else if (to.area() != Area.Tableau && moveCount > 1) {
			throw new Exception("cannot move > 1 to " + to.area());
		}
		
		Card[] newFd;
		Card[] newFr;
		TableauStack[] newT;
		if (to.area() == Area.Foundation) {
			newFd = Arrays.copyOf(tableau._foundation, tableau._foundation.length);
		} else {
			newFd = tableau._foundation;
		}
		
		if (from.area() == Area.Freecell || to.area() == Area.Freecell) {
			newFr = Arrays.copyOf(tableau._freecells, tableau._freecells.length);
		} else {
			newFr = tableau._freecells;
		}
		
		if (from.area() == Area.Tableau || to.area() == Area.Tableau) {
			newT = Arrays.copyOf(tableau._tableau, tableau._tableau.length);
		} else {
			newT = tableau._tableau;
		}
		
		Card c = null;
		switch (from.area()) {
		case Freecell:
			c = newFr[from.column()];
			newFr[from.column()] = null;
			break;
		case Tableau:
			TableauStack tc = newT[from.column()];
			TableauStack ntc = new TableauStack(tc);
			c = ntc.removeCard(tc.stackHeight() - from.offset() - 1);
			newT[from.column()] = ntc;
			break;
		default:
			throw new Exception("unknown Area in from: " + from);
		}
		m.setCard(c);
		
		switch (to.area()) {
		case Foundation:
			newFd[to.column()] = c;
			break;
		case Freecell:
			newFr[to.column()] = c;
			break;
		case Tableau:
			int newLength = newT[to.column()].stackHeight() + 1;
			TableauStack ntc = new TableauStack(newT[to.column()]);
			ntc.addCardToStack(c);
			newT[to.column()] = ntc;
			break;
		default:
			throw new Exception("unknown to Area in Mover.move: " + to);
		}
				
		return new Tableau(newFd, newFr, newT);
	}

	public static boolean isWin(Tableau nt) {
		for (Card c : nt._foundation) {
			if (c == null || c.rank() != Card.KING_RANK) {
				return false;
			}
		}
		
		return true;
	}

	public static void printWin(MoveTree mt) {
		System.out.println("It's a win?");
		Move[] moves = mt.moves();
		int moveNumber = 0;
		for (Move m : moves) {
			System.out.println(String.format("%3d: %s", ++moveNumber, m.shortName()));
		}
	}
}
