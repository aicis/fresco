package dk.alexandra.fresco.suite.dummy.arithmetic;

import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.math.BigInteger;

/**
 * Interface for the Dummy Arithmetic suites ResourcePool.
 * 
 */
public interface DummyArithmeticResourcePool extends ResourcePool {

  /**
   * Get the modulus used.
   * 
   * @return a number used as modulus
   */
  public BigInteger getModulus();

  /**
   * Gets a serializer for BigIntegers send/recieved by the native protocols of the suite.
   * 
   * @return a serialize for BigIntegers
   */
  public BigIntegerSerializer getSerializer();

}
