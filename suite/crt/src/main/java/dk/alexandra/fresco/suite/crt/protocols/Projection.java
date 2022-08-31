package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import java.math.BigInteger;

/**
 * Map (x,y) in CRT coordinates to (x,0) or (0,y) using the LEFT and RIGHT coordinate resp.
 */
public class Projection<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends CRTComputation<SInt, ResourcePoolA, ResourcePoolB> {

  private final Coordinate coordinate;
  private final DRes<SInt> crt;

  public Projection(DRes<SInt> crt, Coordinate coordinate) {
    this.crt = crt;
    this.coordinate = coordinate;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder,
      CRTNumericContext<ResourcePoolA, ResourcePoolB> context) {
    BigInteger coefficient;
    if (coordinate == Coordinate.LEFT) {
      coefficient = context.getRightModulus().multiply(context.getRightModulus().modInverse(context.getLeftModulus()));
    } else {
      coefficient = context.getLeftModulus().multiply(context.getLeftModulus().modInverse(context.getRightModulus()));
    }
    return builder.seq(seq -> seq.numeric().mult(coefficient, crt));
  }

  public enum Coordinate {
    LEFT, RIGHT
  }
}
