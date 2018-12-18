package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Factory for {@link CompT} instances.
 */
public interface CompUIntFactory<CompT extends CompUInt<?, ?, CompT>> extends FieldDefinition {

  /**
   * Creates new {@link CompT} from a raw array of bytes.
   */
  CompT createFromBytes(byte[] bytes);

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

}
