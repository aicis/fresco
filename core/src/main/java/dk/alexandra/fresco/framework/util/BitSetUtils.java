package dk.alexandra.fresco.framework.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * This class contains misc. helper methods for {@link java.util.BitSet} used throughout the
 * TinyTables implementation, but could be useful for other protocol suites.
 */
public class BitSetUtils {


  /**
   * Enforcing non instantiation of a static class by private constructor.
   */
  private BitSetUtils() {
  }

  /**
   * Return a copy of the given BitSet b. Note that BitSets are mutable.
   *
   * @param b a BitSet
   * @return a copy
   */
  public static BitSet copy(BitSet b) {
    return (BitSet) b.clone();
  }

  /**
   * Returns a new BitSet such that the first <i>n</i> bits are set at random using the
   * {@link Random#nextBoolean()} method of the provided {@link Random} instance. Note that if this
   * is to be used in crypto, this should be an instance of {@link SecureRandom}.
   *
   * @param n number of random bits
   * @param random Random to generate bits from
   * @return a BitSet of <code>n</code> random bits
   */
  public static BitSet getRandomBits(int n, Random random) {
    BitSet bits = new BitSet(n);
    for (int i = 0; i < n; i++) {
      bits.set(i, random.nextBoolean());
    }
    return bits;
  }

  /**
   * Return a string representation of the first <i>n</i> bits of the given BitSet.
   *
   * @param bits a BitSet
   * @param n the number of bits to be represented as a string
   * @return a String representation of the first <code>n</code> bits of <code>bits</code>
   */
  public static String toString(BitSet bits, int n) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) {
      sb.append(bits.get(i) ? "1 " : "0 ");
    }
    return sb.toString();
  }

  /**
   * Return a string representation of all the given BitSet until (and including) the highest set
   * bit.
   *
   * @param bits a BitSet
   * @return a String representing <code>bits</code>
   */
  public static String toString(BitSet bits) {
    return toString(bits, bits.length());
  }

  /**
   * Returns the inner product of the two {@link BitSet}, e.g. <i>a<sub>0</sub>b<sub>0</sub>
   * &oplus; ... &oplus; a<sub>n-1</sub>b<sub>n-1</sub></i>.
   *
   * @param a left operand
   * @param b right operand
   * @return the bitwise XOR of <code>a</code> and <code>b</code>
   */
  public static boolean innerProduct(BitSet a, BitSet b) {
    BitSet buffer = copy(a);
    buffer.and(b);
    return isOdd(buffer.cardinality());
  }

  private static boolean isOdd(int n) {
    return (n % 2 != 0);
  }

  /*
   * Misc conversion methods
   */

  /**
   * Converts a list of {@link Boolean} into a {@link BitSet}.
   *
   * @param list a list of Booleans
   * @return a BitSet containing <code>list</code>
   */
  public static BitSet fromList(List<Boolean> list) {
    BitSet bitset = new BitSet(list.size());
    for (int i = 0; i < list.size(); i++) {
      bitset.set(i, list.get(i));
    }
    return bitset;
  }

  /**
   * Converts an array of {@link boolean} into a {@link BitSet}.
   *
   * @param array an array of booleans
   * @return a BitSet containing <code>array</code>
   */
  public static BitSet fromArray(boolean[] array) {
    BitSet bitset = new BitSet(array.length);
    for (int i = 0; i < array.length; i++) {
      bitset.set(i, array[i]);
    }
    return bitset;
  }

  /**
   * Converts of a {@link BitSet} to an array of {@link boolean}.
   * @param bitset a BitSet
   * @param length the number of bits of <code>bitset</code> to convert to array
   * @return an array containing the <code>length</code> first bits of <code>bitset</bitset>
   */
  public static boolean[] toArray(BitSet bitset, int length) {
    if (length < 0) {
      throw new IllegalArgumentException("Size of array must not be negative but was + " + length);
    }
    boolean[] array = new boolean[length];
    for (int i = 0; i < length; i++) {
      array[i] = bitset.get(i);
    }
    return array;
  }

  /**
   * Converts of a {@link BitSet} to a list of {@link Boolean}.
   * @param bitset a BitSet
   * @param n the number of bits of <code>bitset</code> to convert to a list
   * @return a list containing the <code>n</code> first bits of <code>bitset</bitset>
   */
  public static List<Boolean> toList(BitSet bitset, int n) {
    if (n < 0) {
      throw new IllegalArgumentException("Size of list must not be negative but was + " + n);
    }
    List<Boolean> list = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      list.add(bitset.get(i));
    }
    return list;
  }
}
