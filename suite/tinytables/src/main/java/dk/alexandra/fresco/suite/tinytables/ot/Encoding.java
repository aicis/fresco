package dk.alexandra.fresco.suite.tinytables.ot;

import java.util.Arrays;
import java.util.BitSet;

public class Encoding {

  private Encoding() {

  }

	/**
	 * Encode a boolean as a <code>byte</code>. We encode <code>true</code> as 1
	 * and <code>false</code> as 0.
	 *
	 * @param b
	 * @return
	 */
	public static byte encodeBoolean(boolean b) {
		return b ? (byte) 0x01 : (byte) 0x00;
	}

	/**
	 * Encode the first <code>length</code> bits of a BitSet as an array of bytes.
	 *
	 * @param bitset
	 * @param length
	 * @return
	 */
	public static byte[] encodeBitSet(BitSet bitset, int length) {
		int size = length + 7 / 8;
		return Arrays.copyOf(bitset.get(0, length).toByteArray(), size);
	}

	public static BitSet decodeBitSet(byte[] bytes) {
		return BitSet.valueOf(bytes);
	}
}
