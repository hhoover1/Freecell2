package freecellState;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import deck.Card;
import deck.Deck;
import deck.Card.Suit;
import deck.Deal;

public class Tableau {
	public static final int FREECELL_COUNT = 4;
	public static final int TABLEAU_SIZE = 8;
	private static final int MAX_FITNESS_VALUE = 40000 + 4000;

	final Card[] _foundation;
	final Card[] _freecells;
	final Card[][] _tableau;
	
	public Tableau(Card[] fd, Card[] fc, Card[][] t) {
		_foundation = fd;
		_freecells = fc;
		_tableau = t;
		this.sortStacks();
	}
	
	public Tableau(Deck d) {
		_foundation = new Card[Card.Suit.values().length];
		_freecells = new Card[FREECELL_COUNT];
		_tableau = new Card[TABLEAU_SIZE][0];
		this.deal(d);
	}
	
	@SuppressWarnings("unused")
	private Tableau() {
		_foundation = null;
		_freecells = null;
		_tableau = null;
	}

	public Card getFound(Suit suit) {
		return _foundation[suit.ordinal()];
	}
	
	public Card getFree(int idx) {
		return _freecells[idx];
	}
	
	public Card getTableau(int column, int offset) {
		Card[] tabCol = _tableau[column];
		return tabCol[tabCol.length - offset - 1];
	}
	
	public Card get(Location from) throws Exception {
		Card res = null;
		switch (from.area()) {
		case Tableau:
			Card[] col = _tableau[from.column()];
			if (from.offset() != col.length - 1) {
				throw new Exception("get only returns top card! : " + from);
			}
			res = col[from.offset()];
			break;
		case Foundation:
			res = _foundation[from.column()];
			break;
		case Freecell:
			res = _freecells[from.column()];
			break;
		default:
			throw new Exception("unknown area in Location :" + from);	
		}
		
		return res;
	}

	public void deal(Deck deck) {
		int column = 0;
		Deal d = new Deal(deck);
		ArrayList<ArrayList<Card>> stacks = new ArrayList<ArrayList<Card>>();
		for (int ii = 0; ii < TABLEAU_SIZE; ++ii) {
			stacks.add(new ArrayList<Card>(8));
		}
		while (!d.isEmpty()) {
			try {
				Card c = d.next();
				stacks.get(column % TABLEAU_SIZE).add(c);
				column += 1;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Card[] template = new Card[0];
		for (int ii = 0; ii < TABLEAU_SIZE; ++ii) {
			ArrayList<Card> cc = stacks.get(ii);
			Card[] ca = cc.toArray(template);
			_tableau[ii] = ca;
		}

		this.sortStacks();
	}

	public String tableauHash() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(this.toString().getBytes());
			byte[] digest = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append((b & 0xF0) >> 4 + 'A');
				sb.append((b & 0x0F) + 'A');
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return "fail";
	}

	public int fitness() {
		// bestest tableau is one card from done.
		int result = 0;
		int nonEmptyFoundation = 0;
		int totalRetired = 0;
		for (Card al : _foundation) {
			if (al != null) {
				nonEmptyFoundation += 1;
				totalRetired += al.rank();
			}
		}
		// # of non-empty foundation columns - max 40000
		result += 10000 * nonEmptyFoundation;
		
		// total depth of foundation columns -- max 13000
		result += 2500 * totalRetired;

		// number of empty tableau columns -- max 16000
		result += 5000 * this.emptyTableauColumns();

		// partial ordered height
		result += 10 * partialOrderedHeights();
		
		// fully ordered depths
		result += 10 * fullyOrderedDepths();
		
		// tallest ordered stack -- max 7 + 13 == 20
		result += tallestOrderedStack() * 100;
		
		result += stackCardScores();
		
		// subtract # of cards in freecells * factor
		result -= 10 * (((int) Math.pow(2.0, (_freecells.length - this.emptyFreecellCount()))) - 1);

		// invert and return
		return MAX_FITNESS_VALUE - result;
	}

	private int stackCardScores() {
		int result = 0;
		for (int ii = 0; ii < _tableau.length; ++ii) {
			result += stackCardScore(ii);
		}
		
		return result;
	}
	
	public int stackCardScore(int idx) {
		Card[] s = _tableau[idx];
		int result = 0;
		for (Card c : s) {
			result += 13 - c.rank();
		}
		
		return result;
	}
	
	private int emptyTableauColumns() {
		int emptyCount = 0;
		for (Card[] ca : this._tableau) {
			if (ca.length == 0) {
				emptyCount += 1;
			}
		}
		
		return emptyCount;
	}

	private int fullyOrderedDepths() {
		int result = 0;
		for (Card[] stack : _tableau) {
			result += fullyOrderedDepth(stack);
		}
		
		return result;
	}

	private int fullyOrderedDepth(Card[] stack) {
		int result = 0;
		if (stack.length > 0) {
			Card lastCard = stack[stack.length - 1];
			for (int cardIdx = stack.length - 2; cardIdx > 0; --cardIdx) {
				Card c = stack[cardIdx];
				if (lastCard.canBePlacedOn(c)) {
					lastCard = c;
					result += 1;
				}
			}
		}

		return result;
	}
	
	private int partialOrderedHeights() {
		int result = 0;
		
		for (int stackIndex = 0; stackIndex < _tableau.length; ++stackIndex) {
			Card[] stack = _tableau[stackIndex];
			if (stack.length > 0) {
				Card lastCard = stack[0];
				for (int cardIndex = 1; cardIndex < stack.length; ++cardIndex) {
					Card c = stack[cardIndex];
					if (c.rank() > lastCard.rank()) {
						result += cardIndex;
						break;
					}
				}
			}
		}
		
		return result;
	}

	private int tallestOrderedStack() {
		int result = 0;
		for (Card[] ac : _tableau) {
			int topOrderLength = topOrderedLength(ac);
			result = Math.max(result, topOrderLength);
		}

		return result;
	}

	private int topOrderedLength(Card[] ac) {
		int result = ac.length == 0 ? 0 : 1;
		int topIndex = ac.length - 1;
		for (int ii = 0; ii < topIndex; ++ii) {
			Card t = ac[topIndex - ii];
			Card u = ac[topIndex - ii - 1];
			if (t.canBePlacedOn(u)) {
				result += 1;
			} else {
				break;
			}
		}

		return result;
	}

	@Override
	public int hashCode() {
		int res = this.getClass().hashCode();
		for (int ii = 0; ii < _foundation.length; ++ii) {
			Card c = _foundation[ii];
			res += c.hashCode() * (100000 * ii);
		}
		
		for (int ii = 0; ii < _tableau.length; ++ii) {
			Card[] tc = _tableau[ii];
			for (int jj = 0; jj < tc.length; ++jj) {
				Card c = tc[jj];
				res += c.hashCode() * ii;
			}
		}
		
		for (int ii = 0; ii < _freecells.length; ++ii) {
			if (_freecells[ii] != null) {
				res += 10000000 + _freecells[ii].hashCode();
			}
		}

		return res;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tableau)) {
			return false;
		}
		
		Tableau ot = (Tableau) other;
		for (int ii = 0; ii < _freecells.length; ++ii) {
			if (_freecells[ii] == null && ot._freecells[ii] == null) {
				continue;
			} else if (_freecells[ii] == null && (ot._freecells[ii] != null)) {
				return false;
			} else if (ot._freecells[ii] == null && _freecells[ii] != null) {
				return false;
			}

			if (!_freecells[ii].equals(ot._freecells[ii])) {
				return false;
			}
		}

		for (int ii = 0; ii < _foundation.length; ++ii) {
			Card c1 = _foundation[ii];
			Card c2 = ot._foundation[ii];
			if (!(c1.equals(c2))) {
				return false;
			}
		}

		for (int ii = 0; ii < _tableau.length; ++ii) {
			Card[] f1 = _tableau[ii];
			Card[] f2 = ot._tableau[ii];
			if (f1.length != f2.length) {
				return false;
			}
			
			for (int jj = 0; jj < f1.length; ++jj) {
				Card c1 = f1[jj];
				Card c2 = f2[jj];
				if (!(c1.equals(c2))) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');

		// print foundation
		sb.append("\nfoundation:\n");
		for (int ii = 0; ii < _foundation.length; ++ii) {
			Card c = _foundation[ii];
			if (c != null) {
				sb.append(c.shortName());
			} else {
				sb.append("  ");
			}
			sb.append(',');
		}

		// print tableau
		sb.append("\n\ntableau:\n");
		int tallestColumn = this.tallestColumn();
		for (int row = 0; row < tallestColumn; ++row) {
			for (int column = 0; column < _tableau.length; ++column) {
				Card[] t = _tableau[column];
				if (t.length != 0 && row < t.length) {
					Card c = t[row];
					sb.append(c.shortName());
					sb.append(", ");
				} else {
					sb.append("  , ");
				}
			}

			sb.append('\n');
		}

		// print freecells
		sb.append("\nfreecells:\n");
		for (int ii = 0; ii < _freecells.length; ++ii) {
			if (_freecells[ii] == null) {
				sb.append("  , ");
			} else {
				sb.append(_freecells[ii].shortName());
				sb.append(", ");
			}
		}

		sb.append(')');
		return sb.toString();
	}

	private int tallestColumn() {
		int res = Integer.MIN_VALUE;
		for (int ii = 0; ii < _tableau.length; ++ii) {
			Card[] t = _tableau[ii];
			res = Math.max(res, t.length);
		}

		return res;
	}

	public static Tableau fromString(String deckString) {
		Deck d = Deck.deckFrom(deckString);
		Tableau t = new Tableau(d);
		
		return t;
	}

	public static Tableau fromStringNoValidation(String deckString) {
		Deck d = Deck.deckFromNoValidation(deckString);
		Tableau t = new Tableau(d);
		
		return t;
	}

	public Card getTopTableau(int tabCol) {
		int colSize = _tableau[tabCol].length;
		if (colSize == 0) {
			return null;
		}
		
		Card c = _tableau[tabCol][colSize - 1];
		return c;
	}

	public Card foundation(int column) {
		return _foundation[column];
	}
	
	public Card freecell(int column) {
		return _freecells[column];
	}

	public List<Card> getCards(Location from) {
		List<Card> res = new ArrayList<Card>();
		Card[] fromStack = null;

		switch (from.area()) {
		case Foundation:
			Card c = _foundation[from.column()];
			res.add(c);
			break;
		case Tableau:
			fromStack = _tableau[from.column()];
			if (fromStack.length > 0) {
				for (int ii = 0; ii < from.offset() + 1; ++ii) {
					c = fromStack[fromStack.length - ii - 1];
					res.add(c);
				}
			}
			break;
		case Freecell:
			if (_freecells[from.column()] != null) {
				res.add(this._freecells[from.column()]);
			}
			break;
		}

		return res;
	}

	public int emptyFreecellCount() {
		int res = 0;
		for (Card c : _freecells) {
			if (c == null) {
				res += 1;
			}
		}
		return res;
	}
	
	public int firstEmptyFreecell() {
		for (int ii = 0; ii < FREECELL_COUNT; ++ii) {
			if (_freecells[ii] == null) {
				return ii;
			}
		}
		
		return -1;
	}

	// Canonicalize a tableau by sorting the stacks by the top card and
	// moving all freecell cards to the lowest slots
	// this shortens searching by eliminating otherwise duplicate states.
	private void sortStacks() {
		Arrays.sort(_tableau, (Comparator<? super Card[]>) new CompareStacks());
		int placeIdx = 0;
		for (Card c : this._freecells) {
			this._freecells[placeIdx++] = c;
		}
	}

	private class CompareStacks implements Comparator<Card[]> {

		public int compare(Card[] a, Card[] b) {
			return this.compareStacks(a, b);
		}

		private int compareStacks(Card[] a, Card[] b) {
			if (a == null || a.length == 0) {
				return b == null || b.length == 0 ? 0 : -1;
			} else if (b == null || b.length == 0) {
				return 1;
			} else {
				if (a.length != b.length) {
					return b.length - a.length;
				}
				Card ac = a[a.length - 1];
				Card bc = b[b.length - 1];
				return ac.compareTo(bc);
			}
		}
	}
}
