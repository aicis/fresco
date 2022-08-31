package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.crt.suites.ProtocolSuiteProtocolSupplier;
import java.math.BigInteger;

public class CRTNumericContext<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends BasicNumericContext {

  private final BigInteger p, q;
  private final ProtocolSuiteProtocolSupplier<ResourcePoolA> leftProtocolSupplier;
  private final ProtocolSuiteProtocolSupplier<ResourcePoolB> rightProtocolSupplier;

  /**
   * Construct a new BasicNumericContext.
   *
   * @param maxBitLength The maximum length in bits that the numbers in the application will have.
   * @param myId         my party id
   * @param noOfParties  number of parties in computation
   */
  public CRTNumericContext(int maxBitLength, int myId, int noOfParties,
      ProtocolSuiteProtocolSupplier<ResourcePoolA> left, ProtocolSuiteProtocolSupplier<ResourcePoolB> right,
      BigInteger p, BigInteger q) {
    super(maxBitLength, myId, noOfParties, new CRTRingDefinition(p, q), p.bitLength());
    this.p = p;
    this.q = q;
    this.leftProtocolSupplier = left;
    this.rightProtocolSupplier = right;
  }

  /** Get the modulus of the left ring in the RNS representation. */
  public BigInteger getLeftModulus() {
    return p;
  }

  /** Get the modulus of the right ring in the RNS representation. */
  public BigInteger getRightModulus() {
    return q;
  }

  /** Get a {@link ProtocolSuiteProtocolSupplier} for the MPC system on the left ring. */
  public ProtocolSuiteProtocolSupplier<ResourcePoolA> getLeftProtocolSupplier() {
    return leftProtocolSupplier;
  }

  /** Get a {@link ProtocolSuiteProtocolSupplier} for the MPC system on the right ring. */
  public ProtocolSuiteProtocolSupplier<ResourcePoolB> getRightProtocolSupplier() {
    return rightProtocolSupplier;
  }


}
