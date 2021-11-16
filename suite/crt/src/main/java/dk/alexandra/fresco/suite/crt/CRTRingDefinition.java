package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import java.math.BigInteger;

public class CRTRingDefinition extends BigIntegerFieldDefinition {

  private final BigInteger p, q;

  public CRTRingDefinition(BigInteger p, BigInteger q) {
    super(p.multiply(q));
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
