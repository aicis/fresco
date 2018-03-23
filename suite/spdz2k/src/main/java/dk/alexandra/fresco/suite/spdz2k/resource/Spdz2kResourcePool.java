package dk.alexandra.fresco.suite.spdz2k.resource;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kProtocolSuite;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStore;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Common resources for {@link Spdz2kProtocolSuite}.
 *
 * @param <PlainT> the type of the core arithmetic data type, in this case an instance of {@link
 * CompUInt}.
 */
public interface Spdz2kResourcePool<PlainT extends CompUInt<?, ?, PlainT>>
    extends NumericResourcePool {

  /**
   * Returns instance of {@link Spdz2kOpenedValueStore} which tracks all opened, unchecked values.
   */
  Spdz2kOpenedValueStore<PlainT> getOpenedValueStore();

  /**
   * Returns instance of {@link Spdz2kDataSupplier} which provides pre-processed material such as
   * multiplication triples.
   */
  Spdz2kDataSupplier<PlainT> getDataSupplier();

  /**
   * Returns factory for constructing concrete instances of {@link PlainT}, i.e., the class
   * representing the raw element data type.
   */
  CompUIntFactory<PlainT> getFactory();

  /**
   * Returns serializer for instances of {@link PlainT}.
   */
  ByteSerializer<PlainT> getPlainSerializer();

  /**
   * Initializes deterministic joint randomness source. <p>Must be called before any protocols
   * relying on joint randomness are used. Requires a network since a coin tossing protocol is
   * executed to establish a joint random seed. It is guaranteed that the supplied network will be
   * closed upon completion of this method.</p>
   *
   * @param networkSupplier supplier for network to be used in coin-tossing
   * @param drbgGenerator creates drbg given the seed generated via coin-tossing
   * @param seedLength expected length for drbg seed
   */
  void initializeJointRandomness(Supplier<Network> networkSupplier,
      Function<byte[], Drbg> drbgGenerator, int seedLength);

  /**
   * The DRBG is useful for protocols which needs a form of shared randomness where the random bytes
   * are not easily guessed by an adversary. This generator will provide exactly that. For explicit
   * security guarantees, we refer to implementations of {@link dk.alexandra.fresco.framework.util.Drbg}.
   *
   * @return An instance of a DRBG.
   */
  Drbg getRandomGenerator();

  /**
   * A DRBG for generating private randomness locally. <p>The purpose of this class is to avoid
   * initializing a new DRBG whenever a native protocol uses it for local randomness as this can be
   * quite costly.</p>
   */
  Drbg getLocalRandomGenerator();

  /**
   * Returns bit length of maximum representable element.
   */
  int getMaxBitLength();

  /**
   * Converts opened value of underlying arithmetic type to a BigInteger. <p>This may convert the
   * value to a negative value depending on the semantics of the plain text type.</p>
   */
  default BigInteger convertRepresentation(PlainT value) {
    return value.getLeastSignificant().toBigInteger();
  }

  /**
   * Returns serializer for {@link HashBasedCommitment}.
   */
  default ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
    return new HashBasedCommitmentSerializer();
  }

  @Override
  default BigInteger convertRepresentation(BigInteger value) {
    return convertRepresentation(getFactory().createFromBigInteger(value));
  }

}
