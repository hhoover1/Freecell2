package deck;

public class Card implements Comparable<Card>, CardLike {
	public enum Suit {
		Hearts,
		Clubs,
		Diamonds,
		Spades
	}

	public static final int RANK_SIZE = 13;
	public static final int KING_RANK = 13;
	private static final int HIDDEN_KING_RANK = 0;

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
		if (_rank == HIDDEN_KING_RANK) {
			return KING_RANK;
		}
		
		return _rank;
	}
	
	public byte ordinal() {
		int suit = _suit.ordinal();
		int rank = this.rank() - 1;
		byte b = (byte) ((suit * 13) + rank);

		return b;
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
		if ((other == null) || !(other instanceof Card)) {
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
		case 'a':
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
		case 't':
			rank = 10;
			break;
		case 'J':
		case 'j':
			rank = 11;
			break;
		case 'Q':
		case 'q':
			rank = 12;
			break;
		case 'K':
		case 'k':
			rank = 0;
			break;
		default:
			throw new IllegalArgumentException(String.format("unknown rank character (%s)", s1));
		}
		return rank;
	}

	static Suit suitFromChar(String s2) {
		Suit suit;
		switch (s2.charAt(0)) {
		case 'H':
		case 'h':
			suit = Suit.Hearts;
			break;
		case 'C':
		case 'c':
			suit = Suit.Clubs;
			break;
		case 'D':
		case 'd':
			suit = Suit.Diamonds;
			break;
		case 'S':
		case 's':
			suit = Suit.Spades;
			break;
		default:
			throw new IllegalArgumentException("unknown suit");
		}
		return suit;
	}

	public boolean isNextRankOf(CardLike foc) {
		if (this._suit != foc.suit()) {
			return false;
		}
		
		if (this.rank() == 1) {
			return false;
		}

		if (this.rank() == foc.rank() + 1) {
			return true;
		}
		
		return false;
	}

	public boolean isPreviousRankOf(CardLike foc) {
		if (foc.rank() == 1) {
			return false;
		}
		
		if (this.rank() == foc.rank() - 1) {
			return true;
		}
		
		return false;
	}

	public boolean canBePlacedOn(CardLike belowCard) {
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

	private boolean colorOpposite(CardLike belowCard) {
		int thisSuit = this.suit().ordinal();
		int otherSuit = belowCard.suit().ordinal();
		if (thisSuit % 2 == 0) {
			return otherSuit % 2 == 1;
		} else {
			return otherSuit % 2 == 0;
		}
	}

	public int compareTo(Card bc) {
		int rc = this.rank() - bc.rank();
		if (rc != 0) {
			return rc;
		}
		
		int sc = this._suit.ordinal() - bc._suit.ordinal();
		
		return sc;
	}

	@Override
	public Card top() {
		return this;
	}

	@Override
	public Card bottom() {
		return this;
	}

	@Override
	public int size() {
		return 1;
	}
	
	@Override
	public Card cardAt(int index) {
		if (index != 0) {
			throw new ArrayIndexOutOfBoundsException("index must be 0 for Card");
		}
		
		return this;
	}

	@Override
	public CardLike[] split(int where) throws Exception {
		CardLike[] result = new CardLike[2];
		if (where == 0) {
			result[0] = null;
			result[1] = this;
		} else if (where == 1) {
			result[0] = this;
			result[1] = null;
		} else {
			throw new Exception("cannot split a card anywhere except 0 or 1");
		}

		return result;
	}
}
