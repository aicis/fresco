package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes {@link BigInteger} to byte arrays using knowledge about the length
 * of the BigInteger.
 */
public class BigIntegerWithFixedLengthSerializer implements ByteSerializer<BigInteger> {

  private int byteLength;

  /**
   * Creates a new instance that adhere to the interface.
   *
   * @param byteLength the amount of bytes intended to be serialized
   */
  public BigIntegerWithFixedLengthSerializer(int byteLength) {
    this.byteLength = byteLength;
  }

  @Override
  public byte[] serialize(BigInteger obj) {
    byte[] bytes = new byte[byteLength];
    return produceBytes(obj, bytes, 0);
  }

  @Override
  public byte[] serialize(List<BigInteger> objs) {
    byte[] bytes = new byte[byteLength * objs.size()];
    int offset = 0;
    for (BigInteger bigInteger : objs) {
      produceBytes(bigInteger, bytes, offset);
      offset += byteLength;
    }
    return bytes;
  }

  @Override
  public BigInteger deserialize(byte[] data) {
    return new BigInteger(data);
  }

  @Override
  public List<BigInteger> deserializeList(byte[] data) {
    int offset = 0;
    List<BigInteger> result = new ArrayList<>();
    while (offset < data.length) {
      byte[] subArray = new byte[byteLength];
      System.arraycopy(data, offset, subArray, 0, byteLength);
      result.add(new BigInteger(subArray));
      offset += byteLength;
    }
    return result;
  }

  private byte[] produceBytes(BigInteger bigInteger, byte[] bytes, int offset) {
    byte[] bb = bigInteger.toByteArray();
    System.arraycopy(bb, 0, bytes, byteLength - bb.length + offset, bb.length);
    return bytes;
  }

}
