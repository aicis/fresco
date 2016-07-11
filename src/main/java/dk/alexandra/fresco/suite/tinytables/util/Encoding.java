package dk.alexandra.fresco.suite.tinytables.util;

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
	public static byte[] encodeBooleans(boolean[] b) {
		byte[] bytes = new byte[b.length];
		for (int i = 0; i < b.length; i++) {
			bytes[i] = encodeBoolean(b[i]);
		}
		return bytes;
	}
	
	/**
	 * Decode a byte-encoded boolean (see {@link #encodeBoolean(boolean)}.
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
	public static boolean[] decodeBooleans(byte[] b) {
		boolean[] booleans = new boolean[b.length];
		for (int i = 0; i < b.length; i++) {
			booleans[i] = decodeBoolean(b[i]);
		}
		return booleans;
	}
	
}
