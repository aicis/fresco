package dk.alexandra.fresco.suite.marlin.resource;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.Broadcast;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.math.BigInteger;

public interface MarlinResourcePool<T extends BigUInt<T>> extends NumericResourcePool {

  /**
   * Returns instance of {@link MarlinOpenedValueStore} which tracks all opened, unchecked values.
   */
  MarlinOpenedValueStore<T> getOpenedValueStore();

  /**
   * Returns instance of {@link MarlinDataSupplier} which provides pre-processed material such as
   * multiplication triples.
   */
  MarlinDataSupplier<T> getDataSupplier();

  /**
   * Returns factory for constructing concrete instances of {@link T}, i.e., the class representing
   * the raw element data type.
   */
  BigUIntFactory<T> getFactory();

  /**
   * Returns serializer for instances of {@link T}.
   */
  ByteSerializer<T> getRawSerializer();

  /**
   * Creates a new broadcast helper.
   */
  Broadcast createBroadcast(Network network);

  default ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
    return new HashBasedCommitmentSerializer();
  }

  // TODO not clear that this belongs here
  int getOperationalBitLength();

  // TODO not clear that this belongs here
  int getEffectiveBitLength();

  @Override
  default BigInteger convertRepresentation(BigInteger bigInteger) {
    // TODO
    return bigInteger;
  }

}
