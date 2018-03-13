package freecellState;

import java.util.List;

import deck.Card;
import deck.Deck;
import freecellState.Location.Area;

public class TableauHash implements Comparable<TableauHash> {
	private static final int TABLEAU_MAX_CARD_COUNT = Deck.DECKSIZE + Tableau.FREECELL_COUNT + Tableau.FOUNDATION_COUNT;
	private static final int TABLEAU_HASH_SIZE = TABLEAU_MAX_CARD_COUNT * 3 / 4;

	Tableau _tableau;
	byte[] _bits = null;
	int _computedHash = -1;

	TableauHash(Tableau tableau) {
		_tableau = tableau;
	}
	
	// testing
	TableauHash(byte[] bits) {
		_bits = bits;
		_tableau = null;
	}

	private void composeHash() {
		if (_bits != null) {
			return;
		}

		byte[] tempBits = new byte[TABLEAU_MAX_CARD_COUNT];
		int hashIndex = 0;

		for (int ii = 0; ii < Tableau.TABLEAU_SIZE; ++ii) {
			Location from = new Location(Area.Tableau, ii, _tableau.heightOfTableauStack(ii) - 1, -1);
			List<Card> stack = _tableau.getCards(from);
			for (Card c : stack) {
				byte b = c == null ? 0x3F : c.ordinal();
				tempBits[hashIndex++] = b;
			}
		}

		for (int ii = 0; ii < Card.Suit.values().length; ++ii) {
			Card c = _tableau.foundation(ii);
			byte b = c == null ? 0x3F : c.ordinal();
			tempBits[hashIndex++] = b;
		}

		for (int ii = 0; ii < Card.Suit.values().length; ++ii) {
			Card c = _tableau.freecell(ii);
			byte b = c == null ? 0x3F : c.ordinal();
			tempBits[hashIndex++] = b;
		}

		_bits = fourToThreeConversion(tempBits);
		_tableau = null;
	}

	private byte[] fourToThreeConversion(byte[] tempBits) {
		byte[] bits = new byte[TABLEAU_HASH_SIZE];
		int bitIndex = 0;

		for (int ii = 0; ii < tempBits.length; ii += 4) {
			byte b0 = (byte) (tempBits[ii] & 0x3F | (tempBits[ii + 1] & 0x3) << 6);
			byte b1 = (byte) ((tempBits[ii + 1] & 0x3C) >> 2 | (tempBits[ii + 2] & 0xF) << 4);
			byte b2 = (byte) ((tempBits[ii + 2] & 0x30) >> 4 | (tempBits[ii + 3] & 0x3F) << 2);
			bits[bitIndex++] = b0;
			bits[bitIndex++] = b1;
			bits[bitIndex++] = b2;
		}

		return bits;
	}

	@Override
	public int hashCode() {
		if (_computedHash != -1) {
			return _computedHash;
		} else if (_bits == null) {
			composeHash();
		}

		long res = 0;
		int byteOffset = 0;
		for (int ii = 0; ii < _bits.length; ++ii) {
			long b = _bits[ii] << ((byteOffset++ * 8) % 32);
			res ^= b;
		}
		
		_computedHash = (int)res;

		return _computedHash;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TableauHash)) {
			return false;
		}
		
		if (_bits == null) {
			composeHash();
		}
		
		TableauHash oth = (TableauHash)o;
		if (oth._bits == null) {
			oth.composeHash();
		}
		
		for (int ii = 0; ii < _bits.length; ++ii) {
			if (_bits[ii] != oth._bits[ii]) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public int compareTo(TableauHash o) {
		if (this._bits == null) {
			this.composeHash();
		}
		if (o._bits == null) {
			o.composeHash();
		}

		for (int ii = 0; ii < this._bits.length; ++ii) {
			int diff = this._bits[ii] - o._bits[ii];
			if (diff != 0) {
				return diff > 0 ? 1 : -1;
			}
		}

		return 0;
	}
	
	@Override
	public String toString() {
		String cn = this.getClass().getName();
		StringBuilder sb = new StringBuilder(cn.substring(cn.lastIndexOf('.') + 1));
		sb.append('(');
		sb.append(this.hashCode());
		sb.append(", ");
		for (int ii = 0; ii < _bits.length; ++ii) {
			byte cur = _bits[ii];
			sb.append(Integer.toHexString(cur & 0xF));
			sb.append(Integer.toHexString((cur & 0xF0) >> 4));
		}
		sb.append(')');
		
		return sb.toString();
	}
}
