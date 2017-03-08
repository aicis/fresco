package dk.alexandra.fresco.framework.network.converters;

import java.nio.ByteBuffer;

/**
 * Helper class for taking care of handling the sending and receiving of byte[].
 * 
 * @author Kasper Damgaard
 *
 */
public class ByteArrayHelper {

	/**
	 * Helper method which takes a byte array and returns a new array 2 bytes
	 * longer, with the length prepended to the array.
	 * 
	 * @param b
	 *            The array to add a size to.
	 * @return An array containing both the size and the original array.
	 */
	public static byte[] addSize(byte[] b) {
		byte[] length = ByteBuffer.allocate(2).putShort((short) b.length).array();
		byte[] res = new byte[2 + b.length];
		System.arraycopy(length, 0, res, 0, 2);
		System.arraycopy(b, 0, res, 2, b.length);
		return res;
	}

	/**
	 * Helper method which assumes that the wanted byte array was created using
	 * {@link #addSize(byte[])}. It peels off the length bytes and returns the
	 * correct number of bytes back. It is assumed that the length will be at
	 * the current position of the given ByteBuffer.
	 * 
	 * @param buffer
	 *            The container for the byte array.
	 * @return The wanted byte array.
	 */
	public static byte[] getByteObject(ByteBuffer buffer) {
		short length = buffer.getShort();
		byte[] res = new byte[length];
		buffer.get(res);
		return res;
	}

}
