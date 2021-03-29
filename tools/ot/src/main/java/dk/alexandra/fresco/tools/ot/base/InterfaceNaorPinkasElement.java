package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;

public interface InterfaceNaorPinkasElement {

  /**
   * @return the Naor-Pinkas Element as a byte[]
   */
  byte[] toByteArray();

  /**
   * Performs the group operation
   * @param other
   * @return
   */
  InterfaceNaorPinkasElement groupOp(InterfaceNaorPinkasElement other);

  /**
   * Creates the inverse of the Naor-Pinkas Element
   * @return
   */
  InterfaceNaorPinkasElement inverse();


  /**
   * Performs the group operation n-times
   * @param n
   * @return
   */
  InterfaceNaorPinkasElement exponentiation(BigInteger n);


}
