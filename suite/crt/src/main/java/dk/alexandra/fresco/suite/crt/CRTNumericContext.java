package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;

public class CRTNumericContext extends BasicNumericContext {

  private final BigInteger p, q;

  /**
   * Construct a new BasicNumericContext.
   *
   * @param maxBitLength The maximum length in bits that the numbers in the application will have.
   * @param myId         my party id
   * @param noOfParties  number of parties in computation
   */
  public CRTNumericContext(int maxBitLength, int myId, int noOfParties,
      BigInteger p, BigInteger q) {
    super(maxBitLength, myId, noOfParties, new CRTRingDefinition(p, q), 0);
    this.p = p;
    this.q = q;
  }

  public BigInteger getP() {
    return p;
  }

  public BigInteger getQ() {
    return q;
  }
}
