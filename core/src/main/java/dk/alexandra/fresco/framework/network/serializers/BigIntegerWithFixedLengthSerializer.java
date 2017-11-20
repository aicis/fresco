package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;

/**
 * Serializes {@link BigInteger} to byte arrays using knowledge about the length
 * of the BigInteger.
 */
public class BigIntegerWithFixedLengthSerializer implements BigIntegerSerializer {

  private int byteLength;

  @Override
  public byte[] toBytes(BigInteger bigInteger) {
    byte[] bytes = new byte[byteLength];
    byte[] bb = bigInteger.toByteArray();
    System.arraycopy(bb, 0, bytes, byteLength - bb.length, bb.length);
    return bytes;
  }

  @Override
  public BigInteger toBigInteger(byte[] bytes) {
    return new BigInteger(bytes);
  }

  /**
   * Creates a new instance that adhere to the interface.
   *
   * @param byteLength the amount of bytes inteded to be serialized
   */
  public BigIntegerWithFixedLengthSerializer(int byteLength) {
    this.byteLength = byteLength;
  }

}
