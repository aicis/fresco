package dk.alexandra.fresco.framework.network.serializers;

import dk.alexandra.fresco.framework.MPCException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Serializes {@link BigInteger} to byte arrays using knowledge about the length
 * of the BigInteger.
 *
 * @author Kasper Damgaard
 *
 */
public class BigIntegerWithFixedLengthSerializer {

	/**
	 * Serializes the BigInteger's byte array representation. The length is
	 * assumed known by the receiver in advance. NOTE: Can only be used to send
	 * numbers >= 0. Negative numbers will be interpreted as positive.
	 *
	 * @param b
	 *            The BigInteger to serialize.
	 * @return A byte array which can be deserialized by
	 *         {@link #toBigInteger(ByteBuffer)}.
	 */
	public static byte[] toBytes(BigInteger b, int lengthInBytes) {
		byte[] bytes = new byte[lengthInBytes];
		byte[] bb = b.toByteArray();
		System.arraycopy(bb, 0, bytes, lengthInBytes - bb.length, bb.length);

		return bytes;
	}

	/**
	 * Uses {@link #toBytes(BigInteger)} to serialize the given BigIntegers.
	 * NOTE: Can only be used to send numbers >= 0. Negative numbers will be
	 * interpreted as positive.
	 *
	 * @param bs
	 *            The array of BigIntegers to serialize.
	 * @return A byte array serialized representation of the BigIntegers.
	 */
	public static byte[] toBytes(BigInteger[] bs, int lengthInBytes) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (BigInteger b : bs) {
			try {
				bos.write(toBytes(b, lengthInBytes));
			} catch (IOException e) {
				throw new RuntimeException("IOException occured while trying to convert a BigInteger to bytes", e);
			}
		}
		return bos.toByteArray();
	}

	/**
	 * Deserializes to a BigInteger from the assumption that the given
	 * ByteBuffer's position points at the bytes created from
	 * {@link #toBytes(BigInteger)}, meaning the content of the BigInteger bytes
	 * since the length is known aforehand.
	 *
	 * @param buffer
	 *            the ByteBuffer containing the serialized BigInteger.
	 * @return The deserialized BigInteger.
	 */
	public static BigInteger toBigInteger(ByteBuffer buffer, int lengthInBytes) {
        if (buffer.remaining() < lengthInBytes)
            throw new MPCException("Cannot load " + lengthInBytes + "/" + buffer.remaining() + " from byteBuffer " + buffer.toString());

        byte[] content = new byte[lengthInBytes];
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
	public static BigInteger[] toBigIntegers(ByteBuffer buffer, int amount, int lengthInBytes) {
		BigInteger[] res = new BigInteger[amount];
        for (int i = 0; i < amount; i++) {
            res[i] = toBigInteger(buffer, lengthInBytes);
        }
        return res;
	}
}
