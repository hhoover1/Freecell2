package deck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import deck.Card.Suit;

public class Deck {
	public static final int DECKSIZE = 52;

	private Card[] _deck;
	
	public static Deck deckFromNoValidation(String in) {
		Deck res = new Deck();
		ArrayList<Card> cards = new ArrayList<Card>();
		Scanner s = new Scanner(in);
		s.useDelimiter(",");
		while (s.hasNext()) {
			String cs = s.next();
			Card card = Card.cardFrom(cs);
			cards.add(card);
		}
		
		res._deck = cards.toArray(new Card[0]);
		s.close();
		return res;
	}
	
	public static Deck deckFrom(String in) {
		Deck d = deckFromNoValidation(in);
		validate(d);
		return d;
	}
	
	private static void validate(Deck d) {
		if (d.size() != DECKSIZE) {
			throw new IllegalArgumentException ("deck is not " + DECKSIZE + " cards");
		}
		boolean[] cardCheck = new boolean[DECKSIZE];
		for (Card c : d.cards()) {
			int ordinal = c.rank();
			int suit = c.suit().ordinal();
			int v = suit * 13 + (ordinal - 1);
			if (cardCheck[v]) {
				throw new IllegalArgumentException(c.toString() + " is duplicated");
			} else {
				cardCheck[v] = true;
			}
		}
		for (int ii = 0; ii < cardCheck.length; ++ii) {
			if (!cardCheck[ii]) {
				throw new IllegalArgumentException("card " + ii + " is missing!");
			}
		}
	}

	private Card[] cards() {
		return _deck;
	}

	public int size() {
		return this._deck.length;
	}

	public Deck() {
		_deck = new Card[DECKSIZE];
		int pos = 0;
		for (Suit s : Suit.values()) {
			for (int value = 1; value < Card.RANK_SIZE; ++value) {
				_deck[pos++] = new Card(s, value);
			}
			_deck[pos++] = new Card(s, 0);
		}
	}
	
	public Deck(Deck d) {
		_deck = Arrays.copyOf(d._deck, DECKSIZE);
	}
	
	public Card get(int position) {
		return _deck[position];
	}

	public void shuffle() {
		Random r = new Random();
		ArrayList<Card> shuf = new ArrayList<Card>(DECKSIZE);
		ArrayList<Card> from = new ArrayList<Card>(DECKSIZE);
		for (Card c : _deck) {
			from.add(c);
		}
		while (!from.isEmpty()) {
			int ii = r.nextInt(from.size());
			Card c = from.get(ii);
			from.remove(ii);
			shuf.add(c);
		}
		
		_deck = shuf.toArray(_deck);
	}

	String dealString(int position) {
		String cname = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cname.substring(cname.lastIndexOf('.') + 1));
		sb.append('(');
		
		for (int ii = 0; ii < _deck.length; ++ii) {
			if (position == ii) {
				sb.append(" | ");
			}
			Card c = _deck[ii];
			sb.append(c.shortName());
			sb.append(',');
		}
		
		sb.append(')');
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return dealString(-1);
	}
}
