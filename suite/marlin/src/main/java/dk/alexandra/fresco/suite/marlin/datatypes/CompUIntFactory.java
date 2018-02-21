package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import java.math.BigInteger;

public interface CompUIntFactory<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> {

  /**
   * Creates new {@link T} from a raw array of bytes.
   */
  T createFromBytes(byte[] bytes);

  T createFromHigh(H value);

  T createFromLow(L value);

  /**
   * Creates new {@link T} from a {@link BigInteger}.
   */
  default T createFromBigInteger(BigInteger value) {
    return (value == null) ? null : createFromBytes(value.toByteArray());
  }

  /**
   * Creates random {@link T}.
   */
  T createRandom();

//  T createDeterministicRandom(Drbg drbg, int numBytes);

  /**
   * Creates element whose value is zero.
   */
  default T zero() {
    return createFromBytes(new byte[getCompositeBitLength() / Byte.SIZE]);
  }

  /**
   * Creates serializer for {@link T} instances.
   */
  ByteSerializer<T> createSerializer();

  int getCompositeBitLength();

  int getLowBitLength();

  int getHighBitLength();

}
