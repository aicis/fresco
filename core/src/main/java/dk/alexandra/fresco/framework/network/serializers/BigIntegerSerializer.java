package dk.alexandra.fresco.framework.network.serializers;

import java.math.BigInteger;

/**
 * Serializer for big integers.
 */
public interface BigIntegerSerializer {

  /**
   * Converts a big integer to an array of bytes
   *
   * @param bigInteger the integer to convert
   * @return the resulting byte array
   */
  byte[] toBytes(BigInteger bigInteger);

  /**
   * Reads a single BigInteger from a byte array.
   *
   * @param bytes the data
   * @return the converted big integer.
   */
  BigInteger toBigInteger(byte[] bytes);
}
