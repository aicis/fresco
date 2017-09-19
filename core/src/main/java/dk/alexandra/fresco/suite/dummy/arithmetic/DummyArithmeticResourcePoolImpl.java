package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Implements the resource pool needed for the Dummy Arithmetic suite.
 *
 */
public class DummyArithmeticResourcePoolImpl extends ResourcePoolImpl
    implements DummyArithmeticResourcePool {

  private BigInteger modulus;
  private int modulusSize;

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   * 
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   * @param network a network to use for the protocol
   * @param random a random generator
   * @param secRand a secure random generator
   * @param modulus the modulus
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers, Network network, Random random,
      SecureRandom secRand, BigInteger modulus, PerformanceLogger pl) {
    super(myId, noOfPlayers, network, random, secRand, pl);
    this.modulus = modulus;
    this.modulusSize = modulus.toByteArray().length;
  }

  @Override
  public BigInteger getModulus() {
    return this.modulus;
  }

  @Override
  public BigIntegerSerializer getSerializer() {
    return new BigIntegerWithFixedLengthSerializer(modulusSize);
  }

}
