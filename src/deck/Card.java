package deck;

public class Card implements Comparable<Card> {
	public enum Suit {
		Hearts,
		Clubs,
		Diamonds,
		Spades
	}

	public static final int RANK_SIZE = 13;
	public static final int KING_RANK = 0;

	private Suit _suit;
	private int  _rank;
	private String _shortName;
	
	public static Card cardFrom(String cs) {
		
		String s1 = cs.substring(0, 1);
		String s2 = cs.substring(1);
		Suit suit = suitFromChar(s2);
		int rank = rankFromChar(s1);
		
		return new Card(suit, rank);
	}

	public Card(Suit s, int r) {
		if (r < 0 || r > 12) {
			throw new java.lang.IllegalArgumentException(s.toString() + ":" + r);
		}
		
		_suit = s;
		_rank = r;
		_shortName = this.calcShortName();
	}
	
	public Suit suit() {
		return _suit;
	}
	
	public int rank() {
		if (_rank == KING_RANK) {
			return 13;
		}
		
		return _rank;
	}
	
	public String rankName() {
		switch (_rank) {
		case 0:
			return "King";
		case 1:
			return "Ace";
		case 10:
			return "Ten";
		case 11:
			return "Jack";
		case 12:
			return "Queen";
		default:
			return Integer.toString(_rank);
		}
	}
	
	public String shortName() {
		return _shortName;
	}
	
	String calcShortName() {
		String suit = _suit.name();
		String rank = this.rankName();
		String res = rank.substring(0,  1);
		res += suit.substring(0, 1);
		return res;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Card)) {
			return false;
		}
		if (!(this._rank == ((Card) other).rank())) {
			return false;
		}
		return this._suit.equals(((Card) other).suit());
	}
	
	@Override
	public int hashCode() {
		int res = _suit.ordinal() * 13 + _rank;
		return res;
	}
	
	@Override
	public String toString() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');
		sb.append(this.rankName());
		sb.append(" of ");
		sb.append(this._suit.toString());
		sb.append(')');
		return sb.toString();
	}

	static int rankFromChar(String s1) {
		int rank = -1;
		switch (s1.charAt(0)) {
		case 'A':
			rank = 1;
			break;
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			rank = Integer.parseInt(s1);
			break;
		case 'T':
			rank = 10;
			break;
		case 'J':
			rank = 11;
			break;
		case 'Q':
			rank = 12;
			break;
		case 'K':
			rank = 0;
			break;
		default:
			throw new IllegalArgumentException("unknown rank character");
		}
		return rank;
	}

	static Suit suitFromChar(String s2) {
		Suit suit;
		switch (s2.charAt(0)) {
		case 'H':
			suit = Suit.Hearts;
			break;
		case 'C':
			suit = Suit.Clubs;
			break;
		case 'D':
			suit = Suit.Diamonds;
			break;
		case 'S':
			suit = Suit.Spades;
			break;
		default:
			throw new IllegalArgumentException("unknown suit");
		}
		return suit;
	}

	public boolean isNextRankOf(Card foc) {
		if (this._suit != foc._suit) {
			return false;
		}
		
		if (this._rank == 1 && foc._rank == 0) {
			return false;
		}

		if (this._rank == foc._rank + 1) {
			return true;
		}
		
		if (this._rank == 0 && foc._rank == 12) {
			return true;
		}
		
		return false;
	}

	public boolean isPreviousRankOf(Card foc) {
		if (this._rank == 0 && foc._rank == 1) {
			return false;
		}
		
		if (this._rank == foc._rank - 1) {
			return true;
		}
		
		if (this._rank == 0 && foc._rank == 12) {
			return true;
		}
		
		return false;
	}

	public boolean canBePlacedOn(Card belowCard) {
		if (belowCard == null) {
			return true;
		}
		if (this.colorOpposite(belowCard)) {
			if (this.isPreviousRankOf(belowCard)) {
				return true;
			}
		}
		
		return false;
	}

	private boolean colorOpposite(Card belowCard) {
		int thisSuit = this.suit().ordinal();
		int otherSuit = belowCard.suit().ordinal();
		if (thisSuit % 2 == 0) {
			return otherSuit % 2 == 1;
		} else {
			return otherSuit % 2 == 0;
		}
	}

	public int compareTo(Card bc) {
		int rc = this.rankOrdinal() - bc.rankOrdinal();
		if (rc != 0) {
			return rc;
		}
		
		int sc = this._suit.ordinal() - bc._suit.ordinal();
		
		return sc;
	}

	private int rankOrdinal() {
		if (this._rank == KING_RANK) {
			return 13;
		}
		
		return this._rank;
	}
}
