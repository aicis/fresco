package dk.alexandra.fresco.framework.util;

import java.util.BitSet;
import java.util.List;

/**
 * Class for operating on bytes. Can also convert between a byte array and a hex string. 
 */
public class ByteArithmetic {

  // This is supposed to be a "static class", ie no instantiation
  private ByteArithmetic() {}

  /**
   * @return x XOR y
   */
  public static byte xor(byte x, byte y) {
    return (byte) (x ^ y);
  }


  /**
   * It is NOT OK if y=res.
   */
  public static void mult(byte x, byte[] y, byte[] res) {
    for (int i = 0; i < y.length; i++) {
      res[i] = 0;
      if (x == 1) {
        res[i] ^= y[i]; // TODO: why the XOR?
      }
    }
  }

  public static byte not(byte value) {
    if (value == 0) {
      return 1;
    } else {
      return 0;
    }
  }

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
   * Convert hex string to boolean array. 1 --> true, 0 --> false
   * 
   */
  public static Boolean[] toBoolean(String hex) throws IllegalArgumentException {
    if (hex.length() % 2 != 0)
      throw new IllegalArgumentException("Illegal hex string");
    Boolean[] res = new Boolean[hex.length() * 4]; // 8
    // System.out.println("Lenght: " + hex.length());
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
   * Convert boolean array to hex string. true --> 1, false --> 0
   * 
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
    for (int i = 0; i < niceBits.length; i++) {
      binb.append(niceBits[i] ? "1" : "0");
    }
    String bin = binb.toString();
    for (int i = 0; i < bin.length() / 4; i++) {
      String digit = bin.substring(i * 4, i * 4 + 4);
      Integer dec = Integer.parseInt(digit, 2);
      String hexStr = Integer.toHexString(dec);
      // System.out.println("Digit -> " + digit + " --> " + dec + " --> " + hexStr);
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

  public static boolean[] convertArray(Boolean[] in) {
    boolean[] output = new boolean[in.length];
    for (int i = 0; i < in.length; i++) {
      output[i] = in[i].booleanValue();
    }
    return output;
  }

}
