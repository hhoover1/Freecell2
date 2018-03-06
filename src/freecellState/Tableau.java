package freecellState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import deck.Card;
import deck.Card.Suit;
import deck.Deal;
import deck.Deck;

/*
 * class Tableau
 * 
 * Notes on a Tableau
 * 	The Foundation tracks only the top cards of each suit.
 *  The Freecells hold only one card in each cell
 *  The tableau portion is a fixed array of variable arrays.
 *  The TOP card of each of the fixed arrays is the highest index
 *  card in the stack.  The 0 index always holds the bottom
 *  of the stack.  We order it this way so that once cards are
 *  put in the stack, their relative indexes don't change.
 *  
 */

public class Tableau {
	public static final int FREECELL_COUNT = 4;
	public static final int TABLEAU_SIZE = 8;
	private static final int MAX_FITNESS_VALUE = 80000 + 80000 + 4000 + 20000 + 10000;
	private static final int NO_TRAPPED_CARDS = 10000;
	public static final int FOUNDATION_COUNT = Card.Suit.values().length;

	final Card[] _foundation;
	final Card[] _freecells;
	//final Card[][] _tableau;
	final TableauStack[] _tableau;
	private TableauHash _tableauHash;

	public Tableau(Card[] fd, Card[] fc, TableauStack[] newT) {
		_foundation = fd;
		_freecells = fc;
		_tableau = newT;
		this.sortStacks();
	}

	public Tableau(Deck d) {
		_foundation = new Card[Card.Suit.values().length];
		_freecells = new Card[FREECELL_COUNT];
		//_tableau = new Card[TABLEAU_SIZE][0];
		_tableau = new TableauStack[TABLEAU_SIZE];
		for (int colIdx = 0; colIdx < _tableau.length; ++colIdx) {
			_tableau[colIdx] = new TableauStack(colIdx);
		}
		
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

	public Card getCardFromTableau(int column, int offset) {
		TableauStack tabCol = _tableau[column];
		return tabCol.getCard(offset);
	}

	public Card get(Location from) throws Exception {
		Card res = null;
		switch (from.area()) {
		case Tableau:
			TableauStack col = _tableau[from.column()];
			if (from.offset() != col.stackHeight() - 1) {
				throw new Exception("get only returns top card! : " + from);
			}
			res = col.getCard(from.offset());
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
		for (int colIdx = 0; colIdx < TABLEAU_SIZE; ++colIdx) {
			ArrayList<Card> cc = stacks.get(colIdx);
			TableauStack ca = new TableauStack(cc.toArray(template), colIdx);
			_tableau[colIdx] = ca;
		}

		this.sortStacks();
	}

	public TableauHash tableauHash() {
		if (_tableauHash == null) {
			_tableauHash = new TableauHash(this);
		}
		
		return _tableauHash;
	}

	public boolean hasTrappedCard() {
		for (TableauStack col : _tableau) {
			if (trappedCardHeight(col) >= 0) {
				return true;
			}
		}
		
		return false;
	}

	public int trappedCardHeight(int colIdx) {
		TableauStack col = _tableau[colIdx];
		return trappedCardHeight(col);
	}
	
	public int trappedDepths() {
		int depths = 0;
		for (int ii = 0; ii < Tableau.TABLEAU_SIZE; ++ii) {
			int d = this.trappedCardHeight(ii);
			if (d >= 0) {
				depths += this.heightOfTableauStack(ii) - d - 1;
			}
		}
		
		return depths;
	}

	/**
	 * @param col
	 */
	private int trappedCardHeight(TableauStack col) {
		if (col != null && col.stackHeight() > 1) {
			Card lastCard = col.topCard();
			for (int ii = col.stackHeight() - 2; ii >= 0; --ii) {
				Card c = col.getCard(ii);
				if (c.rank() < lastCard.rank()) {
					return ii;
				}
				lastCard = c;
			}
		}
		
		return -1;
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
		//result += 500 * fullyOrderedDepths();
		int[] fullyOrdered = fullyOrderedTableauDepths();
		for (int foc : fullyOrdered) {
			result += foc * foc * 100;
		}

		// tallest ordered stack -- max 7 + 13 == 20
		result += tallestOrderedStack() * 100;

		result += stackCardScores();
		
		if (!this.hasTrappedCard()) {
			result += NO_TRAPPED_CARDS;
		}

		// subtract # of cards in freecells * factor
		result -= 10 * (((int) Math.pow(2.0, (_freecells.length - this.emptyFreecellCount()))) - 1);

		// invert and return
		return MAX_FITNESS_VALUE - result;
	}

	int[] fullyOrderedTableauDepths() {
		int[] res = new int[TABLEAU_SIZE];
		for (int ii = 0; ii < _tableau.length; ++ii) {
			res[ii] = fullyOrderedDepth(_tableau[ii]);
		}
		
		return res;
	}
	
	int stackCardScores() {
		int result = 0;
		for (int ii = 0; ii < _tableau.length; ++ii) {
			result += stackCardScore(ii);
		}

		return result;
	}

	public int stackCardScore(int idx) {
		TableauStack s = _tableau[idx];
		int result = 0;
		for (Card c : s.cards()) {
			result += 13 - c.rank();
		}

		return result;
	}

	int emptyTableauColumns() {
		int emptyCount = 0;
		for (TableauStack ca : this._tableau) {
			if (ca.stackHeight() == 0) {
				emptyCount += 1;
			}
		}

		return emptyCount;
	}

	int fullyOrderedDepths() {
		int result = 0;
		for (TableauStack stack : _tableau) {
			int stackDepth = fullyOrderedDepth(stack);
			if (stackDepth > 1) {
				result += stackDepth;
			}
		}

		return result;
	}

	int fullyOrderedDepth(TableauStack _tableau2) {
		int result = 1;
		if (_tableau2.stackHeight() > 1) {
			Card lastCard = _tableau2.getCard(_tableau2.stackHeight() - 1);
			for (int cardIdx = _tableau2.stackHeight() - 2; cardIdx >= 0; --cardIdx) {
				Card c = _tableau2.getCard(cardIdx);
				if (c.canBePlacedOn(lastCard)) {
					lastCard = c;
					result += 1;
				} else {
					return 0;
				}
			}
		}

		return result;
	}

	int partialOrderedHeights() {
		int result = 0;

		for (int stackIndex = 0; stackIndex < _tableau.length; ++stackIndex) {
			TableauStack stack = _tableau[stackIndex];
			if (stack.stackHeight() > 0) {
				Card lastCard = stack.getCard(0);
				for (int cardIndex = 1; cardIndex < stack.stackHeight(); ++cardIndex) {
					Card c = stack.getCard(cardIndex);
					if (c.rank() > lastCard.rank()) {
						result += cardIndex;
						break;
					}
				}
			}
		}

		return result;
	}

	int tallestOrderedStack() {
		int result = 0;
		for (TableauStack ac : _tableau) {
			int topOrderLength = topOrderedLength(ac);
			result = Math.max(result, topOrderLength);
		}

		return result;
	}

	int topOrderedLength(TableauStack ac) {
		int result = ac.stackHeight() == 0 ? 0 : 1;
		int topIndex = ac.stackHeight() - 1;
		for (int ii = 0; ii < topIndex; ++ii) {
			Card t = ac.getCard(ii);
			Card u = ac.getCard(ii + 1);
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
			TableauStack tc = _tableau[ii];
			for (int jj = 0; jj < tc.stackHeight(); ++jj) {
				Card c = tc.getCard(jj);
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
			TableauStack f1 = _tableau[ii];
			TableauStack f2 = ot._tableau[ii];
			if (f1.stackHeight() != f2.stackHeight()) {
				return false;
			}

			for (int jj = 0; jj < f1.stackHeight(); ++jj) {
				Card c1 = f1.getCard(jj);
				Card c2 = f2.getCard(jj);
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
		sb.append("foundation:\n");
		Card c = _foundation[0];
		if (c != null) {
			sb.append(c.shortName());
		} else {
			sb.append("  ");
		}
		for (int ii = 1; ii < _foundation.length; ++ii) {
			sb.append(',');
			c = _foundation[ii];
			if (c != null) {
				sb.append(c.shortName());
			} else {
				sb.append("  ");
			}
		}

		// print tableau
		sb.append("\n\ntableau:\n");
		TableauStack[] stacks = Arrays.copyOf(_tableau, _tableau.length);
		Arrays.sort(stacks);
		int tallestColumn = this.tallestColumn();
		for (int row = 0; row < tallestColumn; ++row) {
			for (int column = 0; column < stacks.length; ++column) {
				TableauStack t = stacks[column];
				if (t.stackHeight() != 0 && row < t.stackHeight()) {
					Card tc = t.getCard(t.stackHeight() - row - 1);
					sb.append(tc.shortName());
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
			TableauStack t = _tableau[ii];
			res = Math.max(res, t.stackHeight());
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

	public Card getTopOfTableauCol(int tabCol) {
		int colSize = _tableau[tabCol].stackHeight();
		if (colSize == 0) {
			return null;
		}

		Card c = this.getCardFromTableau(tabCol, 0);
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
		TableauStack fromStack = null;

		switch (from.area()) {
		case Foundation:
			Card c = _foundation[from.column()];
			res.add(c);
			break;
		case Tableau:
			fromStack = _tableau[from.column()];
			if (fromStack.stackHeight() > 0) {
				for (int ii = from.offset(); ii >= 0; --ii) {
					c = fromStack.getCard(ii);
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

	public Card[] topTableauCards() {
		Card[] tops = new Card[TABLEAU_SIZE];
		for (int ii = 0; ii < tops.length; ++ii) {
			tops[ii] = this.getTopOfTableauCol(ii);
		}

		return tops;
	}

	// Canonicalize a tableau by sorting the stacks by the top card and
	// moving all freecell cards to the lowest slots
	// this shortens searching by eliminating otherwise duplicate states.
	private void sortStacks() {
		Arrays.sort(_tableau, (Comparator<? super TableauStack>) new CompareStacks());
		int placeIdx = 0;
		for (Card c : this._freecells) {
			this._freecells[placeIdx++] = c;
		}
	}

	private class CompareStacks implements Comparator<TableauStack> {

		public int compare(TableauStack a, TableauStack b) {
			return this.compareStacks(a, b);
		}

		private int compareStacks(TableauStack a, TableauStack b) {
			if (a == null || a.stackHeight() == 0) {
				return b == null || b.stackHeight() == 0 ? 0 : 1;
			} else if (b == null || b.stackHeight() == 0) {
				return -1;
			} else {
				if (a.stackHeight() != b.stackHeight()) {
					return b.stackHeight() - a.stackHeight();
				}
				Card ac = a.getCard(0);
				Card bc = b.getCard(0);
				return ac.compareTo(bc);
			}
		}
	}

	public int heightOfTableauStack(int column) {
		return _tableau[column].stackHeight();
	}

	public int cardsLeft() {
		int result = 52;
		for (Card c : _foundation) {
			if (c != null) {
				result -= c.rank();
			}
		}

		return result;
	}

	void put(Location l, Card c) {
		switch (l.area()) {
		case Tableau:
			TableauStack cs = _tableau[l.column()];
			TableauStack ncs = new TableauStack(cs);
			ncs.insertCard(c, l.offset());
			_tableau[l.column()] = ncs;
			break;
		case Foundation:
			_foundation[l.column()] = c;
			break;
		case Freecell:
			_freecells[l.column()] = c;
			break;
		}
	}

	TableauStack getTableauStack(int i) {
		return _tableau[i];
	}

	public int originalColumn(int tabCol) {
		TableauStack ts = _tableau[tabCol];
		return ts.originalColumn();
	}
}
