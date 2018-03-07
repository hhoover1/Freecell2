package freecellState;

import java.util.Iterator;

/*
 * DeckStringVariations is a class intended to iterate through the possible
 * variations in tail string ordering of the last 8 characters
 * of a deck string.
 * The change in position of the last 8 characters will result in a
 * deck that is canonically identical to the other variations since
 * they are the keys in the deck ordering for the original deck.  They
 * will always end up in the same ordering.  hmmm.. that's not quite
 * true - if there are any equivalent orderings...  but no, as long
 * as suit AND rank are ordered, we're good.
 * so, take the 8th char from the end of the string and produce all
 * 7x6x5x4x3x2 variants.
 * How to generate the variants?
 * recursively - take the highest byte, generate variants below,
 * then take next highest byte as highest (swapping original highest)
 * and generate variants below, etc. until you've put original lowest
 * byte into high position and generated.
 * 
 * This implementation does NOT include the original string in the
 * variations enumeration.
 */
public class DeckStringVariations implements Iterator<String> {
	private final String base;
	private String variation;
	private int changeNumber = 0;
	private int topCharIndex = 1;
	private DeckStringVariations lowerDeckString = null;
	
	DeckStringVariations(String start) {
		base = start;
		variation = start;
		changeNumber = 0;
		update();
	}
	
	DeckStringVariations(String start, int topIndex) {
		base = start;
		variation = start;
		changeNumber = 0;
		topCharIndex = topIndex;
		update();
	}
	
	private void update() {
		if (lowerDeckString == null && topCharIndex > 1) {
			lowerDeckString = new DeckStringVariations(base, topCharIndex);
			topCharIndex += 1;
		}
		
		if (lowerDeckString != null && lowerDeckString.hasNext()) {
			variation = lowerDeckString.next();
		}
	}

	@Override
	public boolean hasNext() {
		return variation != null;
	}

	@Override
	public String next() {
		String result = variation;
		update();
		return variation;
	}
}
