package com.fyre.cobblecuisine.random;

public class RandomNumberGenerator {
	private final long seed;
	private long tick;
	private long state;

	public RandomNumberGenerator(long seed) {
		this.seed = seed;
		this.tick = 0;
		this.state = seed;
	}

	public void onTick() {
		long x = seed + ++tick;
		x ^= (x << 13);
		x ^= (x >>> 7);
		x ^= (x << 17);
		state = x;
	}

	public int nextInt(int min, int max) { return (int) ((Math.abs(state == Long.MIN_VALUE ? 0 : state) % ((max > min) ? (max - min) : 1)) + min); }
	public long nextLong() { return state; }
	public double nextDouble() { return (state >>> 11) * 0x1.0p-53; }
	public float nextFloat() { return (state >>> 40) * 0x1.0p-24f; }

	public static RandomNumberGenerator create() { return new RandomNumberGenerator(System.nanoTime()); }
}
