package dk.alexandra.fresco.framework.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * This class contains misc. helper methods for {@link java.util.BitSet}s used
 * throughout the TinyOT implementation.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class BitSetUtils {

	/**
	 * Return a copy of the given BitSet b. Note that BitSets are mutable.
	 * @param b
	 * @return
	 */
	public static BitSet copy(BitSet b) {
		return (BitSet) b.clone();
	}

	/**
	 * This method returns a combination of the given BitSets. Note that each
	 * BitSet is represented only <i>until</i> the highest set bit.
	 * 
	 * @param sets
	 * @return
	 */
	public static BitSet combine(BitSet... sets) {
		BitSet result = new BitSet();
		int i = 0;
		for (BitSet set : sets) {
			xor(result, set, i);
			i += set.length();
		}
		return result;
	}
	
	/**
	 * XOR the bits from one BitSet onto another, starting specified index in
	 * the <code>to</code> BitSet.
	 * 
	 * @param to
	 * @param from
	 * @param toIndex
	 */
	public static void xor(BitSet to, BitSet from, int startIndex) {
		BitSet buffer = copy(from);
		shiftRight(buffer, startIndex);
		to.xor(buffer);
	}
	
	/**
	 * Returns a new BitSet such that the first <i>n</i> bits are set at random
	 * using the {@link Random#nextBoolean()} method of the provided
	 * {@link Random} instance. Note that if this is to be used in crypto, this
	 * should be an instance of {@link SecureRandom}.
	 * 
	 * @param length
	 * @param random
	 * @return
	 */
	public static BitSet getRandomBits(int n, Random random) {
		BitSet bits = new BitSet(n);
		for (int i = 0; i < n; i++) {
			bits.set(i, random.nextBoolean());
		}
		return bits;
	}

	/**
	 * Return a string representation of the first <i>n</i> bits of the given
	 * BitSet.
	 * 
	 * @param bits
	 * @param n
	 * @return
	 */
	public static String toString(BitSet bits, int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(bits.get(i) ? "1 " : "0 ");
		}
		return sb.toString();
	}
	
	/**
	 * Return a string representation of all the given BitSet until (and
	 * including) the highest set bit.
	 * 
	 * @param bits
	 * @return
	 */
	public static String toString(BitSet bits) {
		return toString(bits, bits.length());
	}

	/**
	 * This method returns the inner product of the two bitsets, eg.
	 * <i>a<sub>0</sub>b<sub>0</sub> &oplus; ... &oplus;
	 * a<sub>n-1</sub>b<sub>n-1</sub></i>.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean innerProduct(BitSet a, BitSet b) {
		BitSet buffer = copy(a);
		buffer.and(b);
		return isOdd(buffer.cardinality());
	}
	
	private static boolean isOdd(int n) {
		return (n % 2 != 0);
	}
	
	/**
	 * Shift the given BitSet <code>shifts</code> bits to the right.
	 * 
	 * @param bits
	 * @param shifts
	 */
	public static void shiftRight(BitSet bits, int shifts) {
		for (int i = bits.length() - 1; i >= 0; i--) {
			bits.set(i + shifts, bits.get(i));
		}
		bits.clear(0, shifts);
	}
	
	/**
	 * Shift the given BitSet <code>shifts</code> bits to the left.
	 * 
	 * @param bits
	 * @param shifts
	 */
	public static void shiftLeft(BitSet bits, int shifts) {
		int l = bits.length();
		for (int i = 0; i < l - shifts; i++) {
			bits.set(i, bits.get(i + shifts));
		}
		bits.clear(l - shifts, l);
	}

	/*
	 * Misc conversion methods
	 */
	
	public static BitSet fromList(List<Boolean> list) {
		BitSet bitset = new BitSet(list.size());
		for (int i = 0; i < list.size(); i++) {
			bitset.set(i, list.get(i));
		}
		return bitset;
	}
	
	public static BitSet fromArray(boolean[] array) {
		BitSet bitset = new BitSet(array.length);
		for (int i = 0; i < array.length; i++) {
			bitset.set(i, array[i]);
		}
		return bitset;
	}
	
	public static boolean[] toArray(BitSet bitset, int length) {
		boolean[] array = new boolean[length];
		for (int i = 0; i < length; i++) {
			array[i] = bitset.get(i);
		}
		return array;
	}
	
	public static List<Boolean> toList(BitSet bitset, int n) {
		List<Boolean> list = new ArrayList<Boolean>(n);
		for (int i = 0; i < n; i++) {
			list.add(bitset.get(i));
		}
		return list;
	}
}
