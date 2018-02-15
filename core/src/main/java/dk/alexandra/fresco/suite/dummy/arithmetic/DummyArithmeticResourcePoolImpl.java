package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.math.BigInteger;

/**
 * Implements the resource pool needed for the Dummy Arithmetic suite.
 */
public class DummyArithmeticResourcePoolImpl extends ResourcePoolImpl
    implements DummyArithmeticResourcePool {

  private BigInteger modulus;
  private int modulusSize;

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating 
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers) {
    this(myId, noOfPlayers, ModulusFinder.findSuitableModulus(128));
  }

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   *
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   * @param modulus the modulus
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers, BigInteger modulus) {
    super(myId, noOfPlayers);
    this.modulus = modulus;
    this.modulusSize = modulus.toByteArray().length;
  }

  @Override
  public BigInteger getModulus() {
    return this.modulus;
  }

  @Override
  public ByteSerializer<BigInteger> getSerializer() {
    return new BigIntegerWithFixedLengthSerializer(modulusSize);
  }
}
