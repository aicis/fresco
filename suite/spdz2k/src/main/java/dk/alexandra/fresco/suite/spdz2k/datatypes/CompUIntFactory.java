package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import java.math.BigInteger;

/**
 * Factory for {@link CompT} instances.
 */
public interface CompUIntFactory<CompT extends CompUInt<?, ?, CompT>> {

  /**
   * Creates new {@link CompT} from a raw array of bytes.
   */
  CompT createFromBytes(byte[] bytes);

  /**
   * Creates new {@link CompT} from a raw array of {@link CompT} instances packed into a byte array.
   * <p>This method avoids copying a chunk of bytes from the overall byte array and is therefore
   * more efficient when dealing with the deserialization of a large of {@link CompT}
   * instances.</p>
   */
  default CompT createFromBytes(byte[] bytes, int chunkIndex, int byteLength) {
    // TODO remove default once done
    throw new UnsupportedOperationException();
  }

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

}
