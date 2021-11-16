package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import java.math.BigInteger;

public class Truncp implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;

  public Truncp(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      DRes<SInt> liftedX1 = new LiftPQProtocol(value).buildComputation(par);
      DRes<SInt> x2 = new Projection(value, Coordinate.RIGHT).buildComputation(par);
      return Pair.lazy(liftedX1, x2);
    }).seq((seq, x) -> {
      CRTRingDefinition ring = (CRTRingDefinition) seq.getBasicNumericContext().getFieldDefinition();
      BigInteger n2 = ring.getP().modInverse(ring.getQ());
      DRes<SInt> y2 = seq.numeric().mult(n2, seq.numeric().sub(x.getSecond(), x.getFirst()));
      DRes<SInt> y1 = new LiftQPProtocol(y2).buildComputation(seq);
      return seq.numeric().add(y1, y2);
    });
  }
}
