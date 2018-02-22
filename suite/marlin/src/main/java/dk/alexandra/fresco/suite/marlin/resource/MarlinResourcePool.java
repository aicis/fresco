package dk.alexandra.fresco.suite.marlin.resource;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.Broadcast;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MarlinResourcePool<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends CompUInt<HighT, LowT, CompT>>
    extends NumericResourcePool {

  /**
   * Returns instance of {@link MarlinOpenedValueStore} which tracks all opened, unchecked values.
   */
  MarlinOpenedValueStore<CompT> getOpenedValueStore();

  /**
   * Returns instance of {@link MarlinDataSupplier} which provides pre-processed material such as
   * multiplication triples.
   */
  MarlinDataSupplier<CompT> getDataSupplier();

  /**
   * Returns factory for constructing concrete instances of {@link CompT}, i.e., the class
   * representing the raw element data type.
   */
  CompUIntFactory<HighT, LowT, CompT> getFactory();

  /**
   * Returns serializer for instances of {@link CompT}.
   */
  ByteSerializer<CompT> getRawSerializer();

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
   * Creates a new broadcast helper. TODO remove?
   */
  Broadcast createBroadcast(Network network);

  default ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
    return new HashBasedCommitmentSerializer();
  }

  // TODO not clear that this belongs here
  int getOperationalBitLength();

  // TODO not clear that this belongs here
  int getEffectiveBitLength();

  default BigInteger convertRepresentation(CompT value) {
    return value.getLeastSignificant().toBigInteger();
  }

}
