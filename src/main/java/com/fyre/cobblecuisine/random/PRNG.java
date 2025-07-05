package com.fyre.cobblecuisine.random;

import net.minecraft.server.MinecraftServer;

public class PRNG {
	public static MinecraftServer server;

	private static final long SEED = System.nanoTime() + 0x9E3779B97F4A7C15L;
	private static long state;

	private static void advance() {
		long x = SEED + server.getTicks();
		x ^= (x << 13);
		x ^= (x >>> 7);
		x ^= (x << 17);
		state = x;
	}

	public static int nextInt(int min, int max) {
		advance();
		return (int) ((Math.abs(state == Long.MIN_VALUE ? 0 : state) % ((max > min) ? (max - min) : 1)) + min);
	}

	public static long nextLong() {
		advance();
		return state;
	}

	public static double nextDouble() {
		advance();
		return (state >>> 11) * 0x1.0p-53;
	}

	public static float nextFloat() {
		advance();
		return (state >>> 40) * 0x1.0p-24f;
	}
}
