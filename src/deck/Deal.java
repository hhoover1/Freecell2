package deck;

public class Deal {
	private Deck _deck;
	private int  _dealPosition;
	
	public Deal(Deck d) {
		_deck = d;
		_dealPosition = 0;
	}
	
	public void reset() {
		_dealPosition = 0;
	}
	
	public boolean isEmpty() {
		return _dealPosition >= _deck.size();
	}
	
	public Card next() throws Exception {
		if (!this.isEmpty()) {
			return _deck.get(_dealPosition++);
		}
		
		throw new Exception("empty deck!");
	}
	
	@Override
	public String toString() {
		return _deck.dealString(_dealPosition);
	}
}
