package dk.alexandra.fresco.framework.util.ot;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import dk.alexandra.fresco.framework.util.BitSetUtils;

public class Encoding {

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
	
	/**
	 * Encode array of booleans as bytes. To decode, use
	 * {@link #decodeBooleans(byte[])}.
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] encodeBooleans(List<Boolean> booleans) {
		int size = booleans.size() + 7 / 8;
		return Arrays.copyOf(BitSetUtils.fromList(booleans).toByteArray(), size);
	}

	/**
	 * Encode array of booleans as bytes. To decode, use
	 * {@link #decodeBooleans(byte[])}.
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] encodeBooleans(boolean[] booleans) {
		int size = booleans.length + 7 / 8;
		return Arrays.copyOf(BitSetUtils.fromArray(booleans).toByteArray(), size);
	}

	/**
	 * Decode a byte-encoded boolean encoded using
	 * {@link #encodeBooleans(boolean[])}.
	 * 
	 * @param b
	 * @return
	 */
	public static boolean decodeBoolean(byte b) {
		return b != 0x00 ? true : false;
	}

	/**
	 * Decode an array of byte-encoded booleans encoded using
	 * {@link #encodeBooleans(boolean[])}.
	 * 
	 * @param b
	 * @return
	 */
	public static boolean[] decodeBooleans(byte[] bytes) {
		BitSet bitset = BitSet.valueOf(bytes);
		return BitSetUtils.toArray(bitset, bytes.length * 8);
	}

}
