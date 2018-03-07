package freecellState;

import java.util.Arrays;

import deck.Card;

/*
 * @Class TableauStack
 * 
 * The purpose of this class is to contain a column of the Tableau.
 * It is responsible for holding the cards of the stack, growing and shrinking
 * as necessary.
 * The top card of a stack is at offset 0 (zero).
 * The bottom card should be at >index< 0 of the array.  That is, when
 * adding a new card to the stack, only the highest index is modified.
 * 
 * TableauStack also tracks the original column number in the Tableau,
 * which is used for printing - keeping the printout looking rational
 * to a user in spite of the sorting done on the tableau columns.
 * 
 */
public class TableauStack implements Comparable<TableauStack> {
	private Card[] cardsInStack;
	private final int originalColumn;
	
	TableauStack(int origCol) {
		cardsInStack = new Card[0];
		this.originalColumn = origCol;
	}
	
	public TableauStack(Card[] array, int origCol) {
		this.cardsInStack = array;
		this.originalColumn = origCol;
	}

	public TableauStack(TableauStack cs) {
		this.cardsInStack = Arrays.copyOf(cs.cardsInStack, cs.stackHeight());
		this.originalColumn = cs.originalColumn;
	}

	public int stackHeight() {
		return cardsInStack.length;
	}
	
	public int originalColumn() {
		return originalColumn;
	}
	
	public Card getCard(int i) {
		return this.cardsInStack[cardsInStack.length - i - 1];
	}

	public Card topCard() {
		return cardsInStack[cardsInStack.length - 1];
	}

	public void addCardToStack(Card c) {
		Card[] nc = Arrays.copyOf(cardsInStack, cardsInStack.length + 1);
		nc[cardsInStack.length] = c;
		cardsInStack = nc;
	}
	
	public Card removeTopCard() {
		return this.removeCard(0);
	}

	public Card removeCard(int i) {
		Card c = this.cardsInStack[cardsInStack.length - i - 1];
		for (int ii = cardsInStack.length - i - 1; ii < cardsInStack.length - 1; ++ii) {
			this.cardsInStack[ii] = this.cardsInStack[ii + 1];
		}
		Card[] nc = Arrays.copyOf(this.cardsInStack, this.cardsInStack.length - 1);
		this.cardsInStack = nc;
		
		return c;
	}

	public static TableauStack[] fromCardArray(Card[][] t) {
		TableauStack[] result = new TableauStack[Tableau.TABLEAU_SIZE];
		for (int colIdx = 0; colIdx < Tableau.TABLEAU_SIZE; ++colIdx) {
			TableauStack ts = new TableauStack(colIdx);
			for (int rowIdx = 0; rowIdx < t[colIdx].length; ++rowIdx) {
				ts.addCardToStack(t[colIdx][rowIdx]);
			}
			result[colIdx] = ts;
		}
		return result;
	}

	final Card[] cards() {
		return this.cardsInStack;
	}

	void putCard(int putIdx, Card card) {
		this.cardsInStack[putIdx] = card;
	}

	public void insertCard(Card c, int offset) {
		this.cardsInStack = Arrays.copyOf(this.cardsInStack, cardsInStack.length + 1);

		int putIdx = this.cardsInStack.length - 1;
		for (int ii = cardsInStack.length - 2; ii >= putIdx - offset; --ii) {
			this.cardsInStack[ii + 1] = this.cardsInStack[ii];
		}
		
		this.cardsInStack[putIdx - offset] = c;
	}
	
	@Override
	public String toString() {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append("(");
		sb.append("{" + originalColumn + "}:" + cardsInStack.length + ":");
		sb.append(Arrays.toString(cardsInStack));
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int compareTo(TableauStack o) {
		return this.originalColumn - o.originalColumn;
	}
}
