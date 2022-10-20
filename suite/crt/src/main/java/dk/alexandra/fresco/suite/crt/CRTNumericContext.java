package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;

import java.math.BigInteger;

public class CRTNumericContext extends BasicNumericContext {

  private final BigInteger p, q;
  private final BuilderFactoryNumeric left;
  private final BuilderFactoryNumeric right;

  /**
   * Construct a new BasicNumericContext.
   *
   * @param maxBitLength The maximum length in bits that the numbers in the application will have.
   * @param myId         my party id
   * @param noOfParties  number of parties in computation
   */
  public CRTNumericContext(int maxBitLength, int myId, int noOfParties,
                           BuilderFactoryNumeric left, BuilderFactoryNumeric right,
                           BigInteger p, BigInteger q) {
    super(maxBitLength, myId, noOfParties, new CRTRingDefinition(p, q), p.bitLength());
    this.p = p;
    this.q = q;
    this.left = left;
    this.right = right;
  }

  /** Get the modulus of the left ring in the RNS representation. */
  public BigInteger getLeftModulus() {
    return p;
  }

  /** Get the modulus of the right ring in the RNS representation. */
  public BigInteger getRightModulus() {
    return q;
  }

  /**
   * Get a {@link ProtocolSuiteProtocolSupplier} for the MPC system on the left ring.
   */
  public BuilderFactoryNumeric getLeft() {
    return left;
  }

  /**
   * Get a {@link ProtocolSuiteProtocolSupplier} for the MPC system on the right ring.
   */
  public BuilderFactoryNumeric getRight() {
    return right;
  }


}
