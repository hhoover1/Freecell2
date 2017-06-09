package explore;

import freecellState.Move;
import freecellState.Tableau;

public class TableauMove {
	private Tableau _tableau;
	private Move _move;
	
	public TableauMove(Tableau t, Move m) {
		_tableau = t;
		_move = m;
	}
	
	public Tableau tableau() {
		return _tableau;
	}
	
	public Move move() {
		return _move;
	}
}
