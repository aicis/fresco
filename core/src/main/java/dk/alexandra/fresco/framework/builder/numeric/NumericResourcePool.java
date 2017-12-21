package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.math.BigInteger;
import java.security.MessageDigest;

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
   * Gets the message digest for this protocol suite invocation.
   *
   * @return the message digest
   */
  MessageDigest getMessageDigest();

  /**
   * Takes a unsigned BigInteger and converts it (reasonable) to a signed version.
   *
   * @param b the unsigned BigInteger
   * @return the signed BigInteger
   */
  default BigInteger convertRepresentation(BigInteger b) {
    BigInteger modulus = getModulus();
    BigInteger actual = b.mod(modulus);
    if (actual.compareTo(modulus.divide(BigInteger.valueOf(2))) > 0) {
      actual = actual.subtract(modulus);
    }
    return actual;
  }
}
