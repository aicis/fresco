package dk.alexandra.fresco.framework.network.serializers;

import java.nio.ByteBuffer;

public class BooleanSerializer {

	/**
	 * Serializes a boolean into a byte[] of size 1.
	 * 
	 * @param b
	 *            the boolean to serialize.
	 * @return The byte array representation of the boolean.
	 */
	public static byte[] toBytes(boolean b) {
		if (b) {
			return new byte[] { 0x01 };
		} else {
			return new byte[] { 0x00 };
		}
	}

	/**
	 * Deserializes a boolean from a single byte. Note that this will increase
	 * the the pointer in the ByteBuffer by one.
	 * 
	 * @param buffer
	 *            The ByteBuffer containting the serialized boolean.
	 * @return The deserialized boolean.
	 */
	public static boolean fromBytes(ByteBuffer buffer) {
		byte b = buffer.get();
		if (b == 0x01) {
			return true;
		} else {
			return false;
		}
	}
}
