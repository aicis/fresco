package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;


public interface InterfaceNaorPinkasElement<T extends InterfaceNaorPinkasElement<T>> {

  /**
   * @return the Naor-Pinkas Element as a byte[]
   */
  byte[] toByteArray();

  /**
   * Performs the group operation
   * @param other
   * @return
   */
  T groupOp(T other);

  /**
   * Creates the inverse of the Naor-Pinkas Element
   * @return
   */
  T inverse();


  /**
   * Performs the group operation n-times
   * @param n
   * @return
   */
  T exponentiation(BigInteger n);


}
