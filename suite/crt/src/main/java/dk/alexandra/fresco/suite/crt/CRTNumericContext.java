package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;
import java.util.function.BiFunction;

public class CRTNumericContext extends BasicNumericContext {

  private final BigInteger p, q;
  private final BiFunction<DRes<SInt>, DRes<SInt>, Computation<BigInteger, ProtocolBuilderNumeric>> mixedAdd;

  /**
   * Construct a new BasicNumericContext.
   *
   * @param maxBitLength The maximum length in bits that the numbers in the application will have.
   * @param myId         my party id
   * @param noOfParties  number of parties in computation
   */
  public CRTNumericContext(int maxBitLength, int myId, int noOfParties,
      BigInteger p, BigInteger q, BiFunction<DRes<SInt>, DRes<SInt>, Computation<BigInteger, ProtocolBuilderNumeric>> mixedAdd) {
    super(maxBitLength, myId, noOfParties, new CRTRingDefinition(p, q), p.bitLength());
    this.p = p;
    this.q = q;
    this.mixedAdd = mixedAdd;
  }

  public BigInteger getP() {
    return p;
  }

  public BigInteger getQ() {
    return q;
  }

  public Computation<BigInteger, ProtocolBuilderNumeric> mixedAdd(DRes<SInt> x, DRes<SInt> y) {
    return mixedAdd.apply(x, y);
  }
}
