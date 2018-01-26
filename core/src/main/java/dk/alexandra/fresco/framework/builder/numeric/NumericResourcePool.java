package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import java.math.BigInteger;

/**
 * Every resource pool must have a set of properties available, primarily
 * the modulus and a BigInteger serialization.
 * <p>
 * This is paired with the {@link ProtocolSuiteNumeric}.
 */
public interface NumericResourcePool extends ResourcePool {

  /**
   * Gets the modulus.
   *
   * @return modulus
   */
  BigInteger getModulus();

  /**
   * Gets a serializer for big integer that is aligned with the current system settings in this
   * invocation - hence byte length of big integer.
   *
   * @return the serializer
   */
  ByteSerializer<BigInteger> getSerializer();

  /**
   * Takes a unsigned BigInteger and converts it (reasonable) to a signed version.
   *
   * @param bigInteger the unsigned BigInteger
   * @return the signed BigInteger
   */
  default BigInteger convertRepresentation(BigInteger bigInteger) {
    BigInteger modulus = getModulus();
    BigInteger actual = bigInteger.mod(modulus);
    if (actual.compareTo(modulus.divide(BigInteger.valueOf(2))) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }
}
