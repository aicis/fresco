package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import java.math.BigInteger;

/** Compute x / p as integer division (rounded down) with a potential error */
public class Truncp<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends CRTComputation<SInt, ResourcePoolA, ResourcePoolB> {

  private final DRes<SInt> value;

  public Truncp(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder,
      CRTNumericContext<ResourcePoolA, ResourcePoolB> context) {

    // The multiplicative inverse of p mod q
    BigInteger n2 = context.getLeftModulus().modInverse(context.getRightModulus());

    return builder.seq(seq -> new LiftPQProtocol<>(value).buildComputation(seq)).seq((seq, liftedX1) -> {
      Numeric rightNumeric = context.rightNumeric(seq);
      DRes<SInt> y2Right = rightNumeric.sub(context.getRight(value), context.getRight(liftedX1));
      y2Right = rightNumeric.mult(n2, y2Right);
      CRTSInt y2 = new CRTSInt(context.leftNumeric(seq).known(0), y2Right);
      DRes<SInt> y1 = new LiftQPProtocol<>(y2).buildComputation(seq);
      return Pair.lazy(y1, y2Right);
    }).seq((seq, y) -> new CRTSInt(context.getLeft(y.getFirst().out()), y.getSecond()));
  }
}
