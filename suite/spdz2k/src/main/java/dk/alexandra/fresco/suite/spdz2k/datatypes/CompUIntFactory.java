package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Factory for {@link CompT} instances.
 */
public interface CompUIntFactory<CompT extends CompUInt<?, ?, CompT>> extends OIntFactory {

  /**
   * Creates new {@link CompT} from a raw array of bytes.
   */
  CompT createFromBytes(byte[] bytes);

  /**
   * Creates random {@link CompT}.
   */
  CompT createRandom();

  /**
   * Get result from deferred and downcast result to {@link CompT}.
   */
  default CompT fromOInt(OInt value) {
    return Objects.requireNonNull((CompT) value);
  }

  default Spdz2kSInt<CompT> toSpdz2kSInt(DRes<SInt> value) {
    return Objects.requireNonNull((Spdz2kSInt<CompT>) value.out());
  }

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
   * Creates new {@link CompT} from a {@link BigInteger}.
   */
  @Override
  default CompT fromBigInteger(BigInteger value) {
    return (value == null) ? null : createFromBytes(value.toByteArray());
  }

  /**
   * Creates element whose value is zero.
   */
  default CompT zero() {
    return fromBigInteger(BigInteger.ZERO);
  }

  /**
   * Creates element whose value is one.
   */
  default CompT one() {
    return fromBigInteger(BigInteger.ONE);
  }

  /**
   * Creates element whose value is two.
   */
  default CompT two() {
    return fromBigInteger(BigInteger.valueOf(2));
  }

}
