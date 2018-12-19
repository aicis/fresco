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

  /**
   * {@inheritDoc}
   *
   * Note that this method does not convert the entire element but only the lower k bits, since the
   * top s bits do not represent the actual value we are computing over.
   */
  @Override
  BigInteger convertToUnsigned(FieldElement value);

  /**
   * {@inheritDoc}
   *
   * See {@link #convertToUnsigned(FieldElement)}.
   */
  @Override
  BigInteger convertToSigned(BigInteger signed);

  /**
   * Note that this method is consistent with {@link #convertToUnsigned(FieldElement)} and {@link
   * #convertToSigned(BigInteger)} in that in only converts the lower k bits and discards the top s
   * bits.
   */
  @Override
  StrictBitVector convertToBitVector(FieldElement fieldElement);

  @Override
  CompT deserialize(byte[] bytes);

  @Override
  default byte[] serialize(FieldElement object) {
    return ((CompT) object).toByteArray();
  }

  @Override
  default byte[] serialize(List<FieldElement> objects) {
    // TODO hack for now until we figure out a clean way to do serialization using the factory only
    // while keeping things compliant with FieldDefinition interface
    List<CompT> correctType = objects.stream().map(x -> (CompT) x).collect(Collectors.toList());
    return getSerializer().serialize(correctType);
  }

  @Override
  default List<FieldElement> deserializeList(byte[] bytes) {
    // TODO hack for now until we figure out a clean way to do serialization using the factory only
    // while keeping things compliant with FieldDefinition interface
    List<CompT> wrongType = getSerializer().deserializeList(bytes);
    return wrongType.stream().map(x -> (FieldElement) x).collect(Collectors.toList());
  }

}
