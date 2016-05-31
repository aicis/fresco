package dk.alexandra.fresco.suite.ninja.util;

public class NinjaUtil {

	/**
	 * Encode a boolean as a <code>byte</code>. We encode <code>true</code> as 1
	 * and <code>false</code> as 0.
	 * 
	 * @param b
	 * @return
	 */
	public static byte encodeBoolean(boolean b) {
		return b ? (byte) 1 : (byte) 0;
	}
	
	/**
	 * Decode a byte-encoded boolean (see {@link #encodeBoolean(boolean)}.
	 * 
	 * @param b
	 * @return
	 */
	public static boolean decodeBoolean(byte b) {
		return b == 0x00 ? false : true;
	}
	
}
