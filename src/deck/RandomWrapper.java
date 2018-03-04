package deck;

import java.util.Random;

public class RandomWrapper implements deck.Random {
	private Random _random;
	
	public RandomWrapper() {
		_random = new Random();
	}
	
	@Override
	public int next() {
		return Math.abs(_random.nextInt());
	}
}
