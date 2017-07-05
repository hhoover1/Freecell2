/**
 * 
 */
package deck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import deck.Card.Suit;

/**
 * @author hhoover
 *
 */
public class CardStack implements CardSet {
	Card[] _cards;

	public CardStack(Card[] cards) {
		if (cards == null) {
			throw new NullPointerException("array must not be null");
		} else if (cards.length == 0) {
			throw new ArrayIndexOutOfBoundsException("array cannot be zero length");
		} else {
			for (Card c : cards) {
				if (c == null) {
					throw new NullPointerException("cards must not be null");
				}
			}
		}
		_cards = cards;
	}

	private CardStack() {
		_cards = new Card[0];
	}

	public static CardSet cardSetFrom(String setString) {
		String[] cards = setString.split(",");
		List<Card> lcs = new ArrayList<Card>();
		for (String s : cards) {
			Card nc = Card.cardFrom(s);
			lcs.add(nc);
		}
		Card[] ncs = lcs.toArray(new Card[0]);

		return new CardStack(ncs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#suit()
	 */
	@Override
	public Suit suit() {
		return this.top().suit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#rank()
	 */
	@Override
	public int rank() {
		return this.top().rank();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#rankName()
	 */
	@Override
	public String rankName() {
		return this.top().rankName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#shortName()
	 */
	@Override
	public String shortName() {
		StringBuilder sb = new StringBuilder(32);
		sb.append('(');
		for (Card c : _cards) {
			sb.append(c.shortName());
			if (!c.equals(_cards[_cards.length - 1])) {
				sb.append(',');
			}
		}

		sb.append(')');
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#isNextRankOf(deck.CardSet)
	 */
	@Override
	public boolean isNextRankOf(CardSet foc) {
		return this.top().isNextRankOf(foc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#isPreviousRankOf(deck.CardSet)
	 */
	@Override
	public boolean isPreviousRankOf(CardSet foc) {
		return this.top().isPreviousRankOf(foc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#canBePlacedOn(deck.CardSet)
	 */
	@Override
	public boolean canBePlacedOn(CardSet belowCard) {
		return this.bottom().canBePlacedOn(belowCard);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#top()
	 */
	@Override
	public Card top() {
		return _cards[_cards.length - 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#bottom()
	 */
	@Override
	public Card bottom() {
		return _cards[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#size()
	 */
	@Override
	public int size() {
		return _cards.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see deck.CardSet#split(int)
	 */
	@Override
	public CardSet[] split(int where) {
		CardSet[] result = new CardSet[2];
		if (where == 1) {
			Card r = this.top();
			Card[] ncards = Arrays.copyOf(_cards, _cards.length - 1);
			result[0] = r;
			result[1] = new CardStack(ncards);
		} else if (where == _cards.length - 1) {
			Card r = this.bottom();
			Card[] ncards = Arrays.copyOfRange(_cards, 1, _cards.length);
			result[0] = new CardStack(ncards);
			result[1] = r;
		} else {
			Card[] c1 = Arrays.copyOf(_cards, where);
			Card[] c2 = Arrays.copyOfRange(_cards, where, _cards.length);
			result[0] = new CardStack(c1);
			result[1] = new CardStack(c2);
		}

		return result;
	}
}
