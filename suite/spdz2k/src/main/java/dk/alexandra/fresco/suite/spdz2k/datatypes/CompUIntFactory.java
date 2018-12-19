package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Factory for {@link CompT} instances.
 */
public interface CompUIntFactory<CompT extends CompUInt<?, ?, CompT>> extends FieldDefinition {

  /**
   * Creates random {@link CompT}.
   */
  CompT createRandom();

  /**
   * Creates serializer for {@link CompT} instances.
   */
  ByteSerializer<CompT> getSerializer();

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
  CompT createElement(int value);

  @Override
  default CompT createElement(String value) {
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
  CompT deserialize(byte[] bytes);

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
    // TODO hack hack hack
    List<CompT> wrongType = getSerializer().deserializeList(bytes);
    return wrongType.stream().map(x -> (FieldElement) x).collect(Collectors.toList());
  }

}
