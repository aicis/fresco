package dk.alexandra.fresco.framework.util;

import java.util.BitSet;
import java.util.List;

/**
 * Class for converting.
 */
public class ByteAndBitConverter {

  // This is supposed to be a "static class", ie no instantiation
  private ByteAndBitConverter() {
  }

  /**
   * Converts long to big-endian byte array.
   */
  public static byte[] toByteArray(long value) {
    byte[] bytes = new byte[8];
    for (int i = 7; i >= 0; i--) {
      bytes[i] = (byte) (value & 0xFF);
      value >>= 8;
    }
    return bytes;
  }

  /**
   * Converts int to big-endian byte array.
   */
  public static byte[] toByteArray(int value) {
    byte[] bytes = new byte[4];
    for (int i = 3; i >= 0; i--) {
      bytes[i] = (byte) (value & 0xFF);
      value >>= 8;
    }
    return bytes;
  }

  /**
   * Converts an int to its bit representation.
   *
   * @param i an integer
   * @return bit representation of i as {@link BitSet}
   */
  public static BitSet intToBitSet(int i) {
    BitSet bs = new BitSet(Integer.SIZE);
    for (int k = 0; k < Integer.SIZE; k++) {
      if ((i & (1 << k)) != 0) {
        bs.set(k);
      }
    }
    return bs;
  }

  /**
   * Convert hex string to boolean array. 1 --> true, 0 --> false.
   */
  public static Boolean[] toBoolean(String hex) throws IllegalArgumentException {
    if (hex.length() % 2 != 0) {
      throw new IllegalArgumentException("Illegal hex string");
    }
    Boolean[] res = new Boolean[hex.length() * 4]; // 8
    for (int i = 0; i < hex.length() / 2; i++) {
      String sub = hex.substring(2 * i, 2 * i + 2);
      int value = Integer.parseInt(sub, 16);
      int numOfBits = 8;
      for (int j = 0; j < numOfBits; j++) {
        boolean val = (value & 1 << j) != 0;
        res[8 * i + (numOfBits - j - 1)] = val;
      }
    }
    return res;
  }

  /**
   * Convert boolean array to hex string. Converts <code>true</code> to <code>1</code> and
   * <code>false</code> to <code>0</code>.
   */
  public static String toHex(boolean[] bits) {
    StringBuilder hex = new StringBuilder();
    boolean[] niceBits = null;
    if (bits.length % 4 == 0) {
      niceBits = bits;
    } else {
      niceBits = new boolean[4 * ((bits.length / 4) + 1)];
      int offset = 4 - (bits.length % 4);
      System.arraycopy(bits, 0, niceBits, offset, bits.length);
    }

    StringBuilder binb = new StringBuilder();
    for (boolean niceBit : niceBits) {
      binb.append(niceBit ? "1" : "0");
    }
    String bin = binb.toString();
    for (int i = 0; i < bin.length() / 4; i++) {
      String digit = bin.substring(i * 4, i * 4 + 4);
      Integer dec = Integer.parseInt(digit, 2);
      String hexStr = Integer.toHexString(dec);
      hex.append(hexStr);
    }
    if (hex.length() % 2 != 0) {
      hex.insert(0, "0");
    }
    return hex.toString();
  }

  public static String toHex(List<Boolean> bits) {
    Boolean[] bitArray = bits.toArray(new Boolean[1]);
    return toHex(convertArray(bitArray));
  }

  private static boolean[] convertArray(Boolean[] in) {
    boolean[] output = new boolean[in.length];
    for (int i = 0; i < in.length; i++) {
      output[i] = in[i].booleanValue();
    }
    return output;
  }

}
