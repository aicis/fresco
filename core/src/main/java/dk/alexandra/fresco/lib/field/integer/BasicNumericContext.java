package dk.alexandra.fresco.lib.field.integer;

import java.math.BigInteger;

/**
 * Holds the most crucial properties about the finite field we are working within.
 */
public class BasicNumericContext {

  private final int maxBitLength;
  private final BigInteger modulus;
  private final int myId;
  private final int noOfParties;


  /**
   * @param maxBitLength The maximum length in bits that the numbers in the application will
   *     have.
   * @param modulus the modules used in the application
   * @param myId my party id
   * @param noOfParties number of parties in computation
   */
  public BasicNumericContext(int maxBitLength, BigInteger modulus, int myId, int noOfParties) {
    this.maxBitLength = maxBitLength;
    this.modulus = modulus;
    this.myId = myId;
    this.noOfParties = noOfParties;
  }

  /**
   * Returns the maximum number of bits a number in the field can contain.
   */
  public int getMaxBitLength() {
    return this.maxBitLength;
  }


  /**
   * Returns the modulus used in the underlying arithmetic protocol suite.
   *
   * @return The modulus used.
   */
  public BigInteger getModulus() {
    return modulus;
  }


  /**
   * Returns the id of the party
   */
  public int getMyId() {
    return myId;
  }

  /**
   * Returns the number of players.
   */
  public int getNoOfParties() {
    return noOfParties;
  }
}
