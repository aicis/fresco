package dk.alexandra.fresco.lib.field.integer;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import java.math.BigInteger;

/**
 * Holds the most crucial properties about the finite field we are working within.
 */
public class BasicNumericContext {

  private final int maxBitLength;
  private final int myId;
  private final int noOfParties;
  private FieldDefinition fieldDefinition;

  /**
   * @param maxBitLength The maximum length in bits that the numbers in the application will
   *     have.
   * @param myId my party id
   * @param noOfParties number of parties in computation
   * @param fieldDefinition the field definition used in the application
   */
  public BasicNumericContext(int maxBitLength, int myId, int noOfParties,
      FieldDefinition fieldDefinition) {
    this.maxBitLength = maxBitLength;
    this.myId = myId;
    this.noOfParties = noOfParties;
    this.fieldDefinition = fieldDefinition;
  }

  /**
   * Returns the maximum number of bits a number in the field can contain.
   */
  public int getMaxBitLength() {
    return this.maxBitLength;
  }

  /**
   * Returns the field definition used in the underlying arithmetic protocol suite.
   *
   * @return The field definition used.
   */
  public FieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  /**
   * Returns the modulus used in the underlying arithmetic protocol suite.
   *
   * @return The modulus used.
   */
  public BigInteger getModulus() {
    return fieldDefinition.getModulus().getBigInteger();
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
