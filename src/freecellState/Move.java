package freecellState;

import deck.Card;
import freecellState.Location.Area;

public class Move implements Comparable<Move> {
	private Card _card;
	private final Location _from;
	private final Location _to;

	public Move(Location f, Location t) {
		_card = null;
		_from = f;
		_to = t;
	}

	public Move(Card c, Location f, Location t) {
		_card = c;
		_from = f;
		_to = t;
	}

	public Card card() {
		return _card;
	}

	public Location from() {
		return _from;
	}

	public Location to() {
		return _to;
	}

	public void setCard(Card c) {
		_card = c;
	}

	@Override
	public String toString() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');
/*		if (_card != null) {
			sb.append(_card);
			sb.append(" from: ");
		}
		sb.append(_from);
		sb.append(", to: ");
		sb.append(_to);
*/		
		sb.append(_from.shortName());
		sb.append(':');
		sb.append(_to.shortName());
		sb.append(')');
		return sb.toString();
	}

	public int compareTo(Move _move) {
		if (this._from.area() == Area.Freecell) {
			return 2;
		}
		if (this._to.area() == Area.Foundation) {
			return 1;
		}
		if (this._to.area() == Area.Freecell) {
			return 5;
		}
		return 3;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Move) {
			Move om = (Move)other;
			if (this.from().equals(om.from())
					&& this.to().equals(om.to())) {
				return true;
			}
		}
		
		return false;
	}

	public String shortName() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');
		if (_card != null) {
			sb.append(_card.shortName());
			sb.append("f:");
		}
		sb.append(_from.shortName());
		sb.append(":");
		sb.append(_to.shortName());
		sb.append(')');
		return sb.toString();
	}
}
