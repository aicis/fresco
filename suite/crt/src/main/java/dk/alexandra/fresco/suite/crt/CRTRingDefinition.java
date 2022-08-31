package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;

public class CRTRingDefinition extends BigIntegerFieldDefinition {

  private final BigInteger p, q;

  public CRTRingDefinition(BigInteger p, BigInteger q) {
    super(p.multiply(q));
    this.p = p;
    this.q = q;
  }

  public BigInteger getLeftModulus() {
    return p;
  }

  public BigInteger getRightModulus() {
    return q;
  }

  /** Compute the RNS representation of an integer. */
  public Pair<BigInteger, BigInteger> integerToRNS(BigInteger x) {
    return Util.mapToCRT(x, p, q);
  }

  /** Given (x,y) in RNS representation, compute the value it represents in this ring. */
  public BigInteger RNStoBigInteger(BigInteger x, BigInteger y) {
    return Util.mapToBigInteger(x, y, p, q);
  }

  /** Given (x,y) in RNS representation, compute the value it represents in this ring. */
  public BigInteger RNStoBigInteger(Pair<BigInteger, BigInteger> rns) {
    return RNStoBigInteger(rns.getFirst(), rns.getSecond());
  }

}
