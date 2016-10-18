package dk.alexandra.fresco.suite.tinytables.util;

import java.util.ArrayList;
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
		byte[] bytes = new byte[booleans.size()];
		for (int i = 0; i < booleans.size(); i++) {
			bytes[i] = encodeBoolean(booleans.get(i));
		}
		return bytes;
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
	public static List<Boolean> decodeBooleans(byte[] bytes) {
		List<Boolean> booleans = new ArrayList<Boolean>();
		for (byte b : bytes) {
			booleans.add(decodeBoolean(b));
		}
		return booleans;
	}
	
}
