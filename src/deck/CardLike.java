/**
 * 
 */
package deck;

/**
 * @author hhoover
 *
 */
public interface CardLike {
	public Card.Suit suit();
	public int rank();
	
	public String rankName();
	public String shortName();

	public boolean isNextRankOf(CardLike foc);
	public boolean isPreviousRankOf(CardLike foc);
	public boolean canBePlacedOn(CardLike belowCard);

	public Card top();
	public Card bottom();
	public int size();
	public Card cardAt(int index);
	
	/*
	 * split returns the cards from the CardSet split into two CardSets
	 * If a Card is split, one part returned will be null
	 */
	public CardLike[] split(int where) throws Exception;
	
	public static CardLike cardsFrom(String cards) throws Exception {
		if (cards == null || cards.isEmpty()) {
			throw new Exception("cards must not be empty or null");
		}
		
		String[] split = cards.split(",");
		if (split.length == 1) {
			return Card.cardFrom(split[0]);
		} else {
			return CardStack.cardsFrom(split);
		}
	}
}
