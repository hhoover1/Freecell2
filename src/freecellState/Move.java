package freecellState;

import deck.Card;

public class Move implements Comparable<Move> {
	private Card _card;
	private final Location _from;
	private final Location _to;
	public static boolean debuggingMove = false;

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

	public String shortName() {
		String cname = this.getClass().getSimpleName();
		StringBuilder sb = new StringBuilder(cname);
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

	@Override
	public String toString() {
		String cname = this.getClass().getSimpleName();
		StringBuilder sb = new StringBuilder(cname);
		sb.append('(');
		
		if (debuggingMove) {
			if (_card != null) {
				sb.append(_card);
				sb.append(" from: ");
			}
			this.appendDebugRep(sb);
		} else {
			this.appendStringRep(sb);
		}
		
		sb.append(')');
		return sb.toString();
	}
	
	public void appendStringRep(StringBuilder sb) {
		sb.append(_from.shortName());
		sb.append(':');
		sb.append(_to.shortName());
	}

	public void appendDebugRep(StringBuilder sb) {
		sb.append(_from.debugShortName());
		sb.append(':');
		sb.append(_to.debugShortName());
	}
	
	public int compareTo(Move _move) {
		int res = this._from.compareTo(_move._from);
		if (res != 0) {
			return res;
		}
		
		res = this._to.compareTo(_move._to);
		return res;
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
	
	@Override
	public int hashCode() {
		int res = _from.hashCode() ^ _to.hashCode();
		return res;
	}
}
