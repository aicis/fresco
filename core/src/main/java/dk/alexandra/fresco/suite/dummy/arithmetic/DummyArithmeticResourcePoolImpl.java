package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.HmacDrbg;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

/**
 * Implements the resource pool needed for the Dummy Arithmetic suite.
 *
 */
public class DummyArithmeticResourcePoolImpl extends ResourcePoolImpl
    implements DummyArithmeticResourcePool {

  private BigInteger modulus;
  private int modulusSize;

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite. Uses the default
   * {@link dk.alexandra.fresco.framework.util.Drbg} implementation of
   * {@link dk.alexandra.fresco.framework.util.HmacDrbg}.
   * 
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   * @param modulus the modulus
   * @throws NoSuchAlgorithmException If the default HMac algorithm is not available on the system.
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers, BigInteger modulus)
      throws NoSuchAlgorithmException {
    this(myId, noOfPlayers, new HmacDrbg(), modulus);
  }

  /**
   * Constructs a new {@link ResourcePool} for the Dummy Arithmetic suite.
   * 
   * @param myId id of this party
   * @param noOfPlayers number of parties in the participating
   * @param drbg The DRBG used.
   * @param modulus the modulus
   */
  public DummyArithmeticResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg, BigInteger modulus) {
    super(myId, noOfPlayers, drbg);
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
