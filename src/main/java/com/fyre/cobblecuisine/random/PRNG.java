package com.fyre.cobblecuisine.random;

import java.util.Random;

public class PRNG {
	private static volatile Random random = new Random();

	/** Start a fresh random stream without retaining the server instance. */
	public static void reset() {
		random = new Random();
	}

	/** The upper bound is exclusive; preserve the existing degenerate-range fallback. */
	public static int nextInt(int min, int max) {
		return max > min ? random.nextInt(min, max) : min;
	}

	public static long nextLong() {
		return random.nextLong();
	}

	public static double nextDouble() {
		return random.nextDouble();
	}

	public static float nextFloat() {
		return random.nextFloat();
	}
}
