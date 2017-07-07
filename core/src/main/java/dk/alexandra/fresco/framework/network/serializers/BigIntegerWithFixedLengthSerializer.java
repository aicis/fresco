package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Serializes {@link BigInteger} to byte arrays using knowledge about the length
 * of the BigInteger.
 *
 * @author Kasper Damgaard
 */
public class BigIntegerWithFixedLengthSerializer implements BigIntegerSerializer {

  private int byteLength;

  /**
   * Serializes the BigInteger's byte array representation. The length is
   * assumed known by the receiver in advance. NOTE: Can only be used to send
   * numbers >= 0. Negative numbers will be interpreted as positive.
   *
   * @param b The BigInteger to serialize.
   * @return A byte array which can be deserialized by {@link #toBigInteger(ByteBuffer)}.
   */
  public static byte[] toBytes(BigInteger b, int lengthInBytes) {
    byte[] bytes = new byte[lengthInBytes];
    byte[] bb = b.toByteArray();
    System.arraycopy(bb, 0, bytes, lengthInBytes - bb.length, bb.length);

    return bytes;
  }

  /**
   * Deserializes to a BigInteger from the assumption that the given
   * ByteBuffer's position points at the bytes created from
   * {@link #toBytes(BigInteger)}, meaning the content of the BigInteger bytes
   * since the length is known aforehand.
   *
   * @param buffer the ByteBuffer containing the serialized BigInteger.
   * @return The deserialized BigInteger.
   */
  static BigInteger toBigInteger(ByteBuffer buffer, int lengthInBytes) {
    byte[] content = new byte[lengthInBytes];
    buffer.get(content);
    return new BigInteger(content);
  }

  /**
   * Creates a new instance that adhere to the interface.
   *
   * @param byteLength the amount of bytes inteded to be serialized
   */
  public BigIntegerWithFixedLengthSerializer(int byteLength) {
    this.byteLength = byteLength;
  }

  @Override
  public byte[] toBytes(BigInteger bigInteger) {
    return toBytes(bigInteger, byteLength);
  }

  @Override
  public BigInteger toBigInteger(ByteBuffer byteBuffer) {
    return toBigInteger(byteBuffer, byteLength);
  }
}
