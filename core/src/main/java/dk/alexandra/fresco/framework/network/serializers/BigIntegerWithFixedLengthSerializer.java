package dk.alexandra.fresco.framework.network.serializers;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes {@link BigInteger} to byte arrays using knowledge about the length
 * of the BigInteger.
 */
public class BigIntegerWithFixedLengthSerializer implements ByteSerializer<FieldElement> {

  private final int byteLength;
  private final FieldDefinition fieldDefinition;

  /**
   * Creates a new instance that adhere to the interface.
   *
   * @param byteLength the amount of bytes intended to be serialized
   * @param fieldDefinition definition of field bound to the implementation
   */
  public BigIntegerWithFixedLengthSerializer(int byteLength, FieldDefinition fieldDefinition) {
    this.byteLength = byteLength;
    this.fieldDefinition = fieldDefinition;
  }

  @Override
  public byte[] serialize(FieldElement obj) {
    byte[] bytes = new byte[byteLength];
    return produceBytes(obj, bytes, 0);
  }

  @Override
  public byte[] serialize(List<FieldElement> objs) {
    byte[] bytes = new byte[byteLength * objs.size()];
    int offset = 0;
    for (FieldElement bigInteger : objs) {
      produceBytes(bigInteger, bytes, offset);
      offset += byteLength;
    }
    return bytes;
  }

  @Override
  public FieldElement deserialize(byte[] data) {
    return fieldDefinition.deserialize(data, 0, byteLength);
  }

  @Override
  public List<FieldElement> deserializeList(byte[] data) {
    int offset = 0;
    List<FieldElement> result = new ArrayList<>();
    while (offset < data.length) {
      byte[] subArray = new byte[byteLength];
      System.arraycopy(data, offset, subArray, 0, byteLength);
      result.add(deserialize(subArray));
      offset += byteLength;
    }
    return result;
  }

  private byte[] produceBytes(FieldElement bigInteger, byte[] bytes, int offset) {
    bigInteger.toByteArray(bytes, offset, byteLength);
    return bytes;
  }
}
