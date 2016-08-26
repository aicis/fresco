package dk.alexandra.fresco.suite.tinytables.util;

import java.security.SecureRandom;
import java.util.Random;


public class RandomSourceImpl implements RandomSource {

	private static RandomSource instance;

	public static RandomSource getInstance() {
		if (instance == null) {
			instance = new RandomSourceImpl(new SecureRandom());
		}
		return instance;
	}

	private Random random;
	
	public RandomSourceImpl(Random random) {
		this.random = random;
	}

	@Override
	public boolean getRandomBoolean() {
		return random.nextBoolean();
	}

	@Override
	public boolean[] getRandomBooleans(int n) {
		boolean[] r = new boolean[n];
		for (int i = 0; i < n; i++) {
			r[i] = getRandomBoolean();
		}
		return r;
	}
}
