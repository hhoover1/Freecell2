/**
 * 
 */
package deck;

/**
 * @author hhoover
 *
 */
public interface CardSet {
	public Card.Suit suit();
	public int rank();
	
	public String rankName();
	public String shortName();

	public boolean isNextRankOf(CardSet foc);
	public boolean isPreviousRankOf(CardSet foc);
	public boolean canBePlacedOn(CardSet belowCard);

	public Card top();
	public Card bottom();
	public int size();
	
	/*
	 * split returns the cards from the CardSet split into two CardSets
	 * If a Card is split, one part returned will be null
	 */
	public CardSet[] split(int where) throws Exception;
}
