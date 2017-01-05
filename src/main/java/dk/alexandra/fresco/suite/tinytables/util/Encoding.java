package dk.alexandra.fresco.suite.tinytables.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

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
	 * Encode array of booleans as bytes. See also {@link #encodeBoolean(boolean)}.
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] encodeBooleans(List<Boolean> booleans) {
		int size = booleans.size() + 7 / 8;
		return Arrays.copyOf(BitSetUtils.fromList(booleans).toByteArray(), size);
	}
	
	/**
	 * Encode array of booleans as bytes. See also {@link #encodeBoolean(boolean)}.
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] encodeBooleans(boolean[] booleans) {
		int size = booleans.length + 7 / 8;
		return Arrays.copyOf(BitSetUtils.fromArray(booleans).toByteArray(), size);
	}
	
	/**
	 * Decode a byte-encoded boolean. See {@link #encodeBoolean(boolean)}.
	 * 
	 * @param b
	 * @return
	 */
	public static boolean decodeBoolean(byte b) {
		return b != 0x00 ? true : false;
	}
	
	/**
	 * Decode an array of byte-encoded booleans. See also
	 * {@link #decodeBoolean(byte)}.
	 * 
	 * @param b
	 * @return
	 */
	public static boolean[] decodeBooleans(byte[] bytes) {
		BitSet bitset = BitSet.valueOf(bytes);
		return BitSetUtils.toArray(bitset, bytes.length * 8);
	}
	
}
