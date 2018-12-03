package dk.alexandra.fresco.framework.network.serializers;

import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Serializes {@link BigInteger} to byte arrays using knowledge about the length
 * of the BigInteger.
 */
public class BigIntegerWithFixedLengthSerializer implements ByteSerializer<BigIntegerI> {

  private final int byteLength;
  private final Function<byte[], BigIntegerI> bigIntegerSupplier;

  /**
   * Creates a new instance that adhere to the interface.
   *
   * @param byteLength the amount of bytes intended to be serialized
   * @param bigIntegerSupplier the bigInteger deserialization bound to the implementation
   */
  public BigIntegerWithFixedLengthSerializer(
      int byteLength, Function<byte[], BigIntegerI> bigIntegerSupplier) {
    this.byteLength = byteLength;
    this.bigIntegerSupplier = bigIntegerSupplier;
  }

  @Override
  public byte[] serialize(BigIntegerI obj) {
    byte[] bytes = new byte[byteLength];
    return produceBytes(obj, bytes, 0);
  }

  @Override
  public byte[] serialize(List<BigIntegerI> objs) {
    byte[] bytes = new byte[byteLength * objs.size()];
    int offset = 0;
    for (BigIntegerI bigInteger : objs) {
      produceBytes(bigInteger, bytes, offset);
      offset += byteLength;
    }
    return bytes;
  }

  @Override
  public BigIntegerI deserialize(byte[] data) {
    return bigIntegerSupplier.apply(data);
  }

  @Override
  public List<BigIntegerI> deserializeList(byte[] data) {
    int offset = 0;
    List<BigIntegerI> result = new ArrayList<>();
    while (offset < data.length) {
      byte[] subArray = new byte[byteLength];
      System.arraycopy(data, offset, subArray, 0, byteLength);
      result.add(bigIntegerSupplier.apply(subArray));
      offset += byteLength;
    }
    return result;
  }

  private byte[] produceBytes(BigIntegerI bigInteger, byte[] bytes, int offset) {
    byte[] bb = bigInteger.toByteArray();
    System.arraycopy(bb, 0, bytes, byteLength - bb.length + offset, bb.length);
    return bytes;
  }
}
