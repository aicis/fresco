package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Factory for {@link CompT} instances.
 */
public interface CompUIntFactory<CompT extends CompUInt<?, ?, CompT>> extends FieldDefinition {

  /**
   * Creates new {@link CompT} from a raw array of bytes.
   */
  @Override
  CompT deserialize(byte[] bytes);

  /**
   * Creates random {@link CompT}.
   */
  CompT createRandom();

  /**
   * Creates serializer for {@link CompT} instances.
   */
  ByteSerializer<CompT> createSerializer();

  /**
   * Get length of most significant bits which represent the masking portion.
   */
  int getHighBitLength();

  /**
   * Get length of least significant bits which represent the data portion.
   */
  int getLowBitLength();

  /**
   * Get total bit length.
   */
  default int getCompositeBitLength() {
    return getHighBitLength() + getLowBitLength();
  }

  /**
   * Creates element whose value is zero.
   */
  CompT zero();

  /**
   * Creates new {@link CompT} from a {@link BigInteger}.
   */
  @Override
  CompT createElement(BigInteger value);

  @Override
  FieldElement createElement(int value);

  @Override
  default FieldElement createElement(String value) {
    return createElement(new BigInteger(value));
  }

  @Override
  BigInteger getModulus();

  @Override
  default int getBitLength() {
    return getCompositeBitLength();
  }

  @Override
  default StrictBitVector convertToBitVector(FieldElement fieldElement) {
    throw new NotImplementedException();
  }

  @Override
  BigInteger convertToUnsigned(FieldElement value);

  @Override
  BigInteger convertToSigned(BigInteger signed);

  @Override
  default byte[] serialize(FieldElement object) {
    return ((CompT) object).toByteArray();
  }

  @Override
  default byte[] serialize(List<FieldElement> objects) {
    int byteLength = getCompositeBitLength() / Byte.SIZE;
    byte[] all = new byte[byteLength * objects.size()];
    for (int i = 0; i < objects.size(); i++) {
      byte[] serialized = serialize(objects.get(i));
      System.arraycopy(serialized, 0, all, i * byteLength, byteLength);
    }
    return all;
  }

  @Override
  default List<FieldElement> deserializeList(byte[] bytes) {
    int byteLength = getCompositeBitLength() / Byte.SIZE;
    if (bytes.length % byteLength != 0) {
      throw new IllegalArgumentException(
          "Total number of bytes must be a multiple of length of single element");
    }
    int numElements = bytes.length / byteLength;
    List<FieldElement> elements = new ArrayList<>(numElements);
    for (int i = 0; i < numElements; i++) {
      final byte[] thisElementBytes = Arrays.copyOfRange(
          bytes,
          i * byteLength,
          (i + 1) * byteLength);
      elements.add(deserialize(thisElementBytes));
    }
    return elements;
  }

}
