package dk.alexandra.fresco.framework.network.serializers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BigIntegerSerializer {

	/**
	 * Writes both the length of the BigInteger's byte array representation as
	 * well as the representation itself. The length is written as a short to
	 * save space, which means the maximum bit size of the BigInteger can be
	 * maximum 524288 bits.
	 * 
	 * @param b
	 *            The BigInteger to serialize.
	 * @return A byte array which can be deserialized by
	 *         {@link #toBigInteger(ByteBuffer)}.
	 */
	public static byte[] toBytes(BigInteger b) {
		byte[] bytes = b.toByteArray();
		ByteBuffer buf = ByteBuffer.allocate(2 + bytes.length);
		buf.putShort((short) bytes.length);
		buf.put(bytes);
		return buf.array();
	}

	/**
	 * Uses {@link #toBytes(BigInteger)} to serialize the given BigIntegers.
	 * 
	 * @param bs
	 *            The array of BigIntegers to serialize.
	 * @return A byte array serialized representation of the BigIntegers.
	 */
	public static byte[] toBytes(BigInteger[] bs) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (BigInteger b : bs) {
			try {
				bos.write(toBytes(b));
			} catch (IOException e) {
				throw new RuntimeException("IOException occured while trying to convert a BigInteger to bytes", e);
			}
		}
		return bos.toByteArray();
	}

	/**
	 * Deserializes to a BigInteger from the assumption that the given
	 * ByteBuffer's position points at the bytes created from
	 * {@link #toBytes(BigInteger)}, meaning the size of the BigInteger comes
	 * next.
	 * 
	 * @param buffer
	 *            the ByteBuffer container pointing at the position where the
	 *            size is located.
	 * @return The deserialized BigInteger.
	 */
	public static BigInteger toBigInteger(ByteBuffer buffer) {
		short size = buffer.getShort();
		byte[] content = new byte[size];
		buffer.get(content);
		return new BigInteger(content);
	}

	/**
	 * Deserializes into the given {@code amount} of BigIntegers. This method
	 * assumes the serialization was done using {@link #toBytes(BigInteger[])}.
	 * 
	 * @param buffer
	 *            The ByteBuffer containing the serialized bytes.
	 * @param amount
	 *            The amount of BigIntegers to deserialize.
	 * @return An array of size {@code amount} containing deserialized
	 *         BigIntegers.
	 */
	public static BigInteger[] toBigIntegers(ByteBuffer buffer, int amount) {
		BigInteger[] res = new BigInteger[amount];
		for (int i = 0; i < amount; i++) {
			res[i] = toBigInteger(buffer);
		}
		return res;
	}
}
