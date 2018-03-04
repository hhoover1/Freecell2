package freecellState;

import java.util.Arrays;

import deck.Card;

public class TableauStack {
	private Card[] cardsInStack;
	private final int originalColumn;
	
	TableauStack(int origCol) {
		cardsInStack = new Card[0];
		this.originalColumn = origCol;
	}
	
	public int stackHeight() {
		return cardsInStack.length;
	}
	
	public int originalColumn() {
		return originalColumn;
	}
	
	public void addCardToStack(Card c) {
		Card[] nc = Arrays.copyOf(cardsInStack, cardsInStack.length + 1);
		nc[cardsInStack.length] = c;
		cardsInStack = nc;
	}
	
	public Card removeTopCard() {
		return this.removeCard(this.stackHeight() - 1);
	}

	public Card removeCard(int i) {
		Card c = this.cardsInStack[i];
		for (int ii = i; ii < this.cardsInStack.length - 1; ++ii) {
			this.cardsInStack[ii] = this.cardsInStack[ii + 1];
		}
		Card[] nc = Arrays.copyOf(this.cardsInStack, this.cardsInStack.length - 1);
		this.cardsInStack = nc;
		
		return c;
	}
}
