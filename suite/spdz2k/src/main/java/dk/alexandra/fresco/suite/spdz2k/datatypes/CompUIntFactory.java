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

  @Override
  default BigInteger toBigInteger(OInt value) {
    // TODO test
    return ((CompUInt) value).toBigInteger();
  }

  @Override
  default long toLong(OInt value) {
    return ((CompUInt) value).toLong();
  }

  @Override
  default OInt fromBigInteger(BigInteger value) {
    return createFromBigInteger(value);
  }

  @Override
  default OInt fromLong(long value) {
    // TODO rethink this
    return fromBigInteger(BigInteger.valueOf(value));
  }

  /**
   * Get result from deferred and downcast result to {@link CompT}.
   */
  default CompT fromOInt(DRes<OInt> value) {
    return (CompT) value.out();
  }

  /**
   * Get result from deferred and downcast result to {@link Spdz2kSInt<CompT>}.
   */
  default Spdz2kSInt<CompT> toSpdz2kSInt(DRes<SInt> value) {
    return Objects.requireNonNull((Spdz2kSInt<CompT>) value.out());
  }

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
   * Creates new {@link CompT} from a {@link BigInteger}.
   */
  default CompT createFromBigInteger(BigInteger value) {
    return (value == null) ? null : createFromBytes(value.toByteArray());
  }

  /**
   * Creates element whose value is zero.
   */
  default CompT zero() {
    return createFromBytes(new byte[getCompositeBitLength() / Byte.SIZE]);
  }

  default CompT one() {
    return createFromBigInteger(BigInteger.ONE);
  }

  default CompT two() {
    return createFromBigInteger(BigInteger.valueOf(2));
  }

}
