package control;

import deck.Deck;
import explore.MoveTree;
import explore.TableauMoveIterator;
import freecellState.Tableau;

public class PureDepthFirstSolver {
	private static final String DECK_38 = "2H,JS,KC,4C,3D,AH,QC,AS,8H,QH,6S,3C,6C,4H,4S,TS,5C,5D,7C,6H,4D,7D,KH,KD,5S,5H,3H,9D,7H,JC,KS,9C,8C,8D,JH,2D,9H,JD,QS,QD,6D,8S,2C,TH,7S,TC,AC,9S,AD,TD,2S,3S";
	private static Deck d = Deck.deckFrom(DECK_38);
	private static Tableau startTableau = new Tableau(d);
	
	public static void main(String[] args) {
		MoveTree base = new MoveTree();
		TableauMoveIterator tmi = new TableauMoveIterator(startTableau, base, 150, 0);
		int count = 0;
		int countSinceLowScore = 0;
		while (tmi.hasNext()) {
			MoveTree m = tmi.next();
			count += 1;
			if (count % 100000 == 0) {
				System.out.println(String.format("%8d: %s-%5d", count, m, tmi.checkedStates()));
			}
			
			if (count % 1000000 == 0) {
				System.out.println(String.format("%8d:: %s-%5d\n%s", count, m, tmi.checkedStates(), tmi.tableau()));
			}
			
			if (m.depth() == 30) {
				System.out.println(">depth 30!!!");
				System.out.println(String.format("%8d:: %s-%5d\n%s", count, m, tmi.checkedStates(), tmi.tableau()));
				System.out.println("<depth 30!!!");
			}
			
			if (++countSinceLowScore > 10000 && m.score() < 0) {
				System.out.println(String.format("%8d:: %s-%5d\n%s", count, m, tmi.checkedStates(), tmi.tableau()));
				countSinceLowScore = 0;
			}
		}
	}
}
