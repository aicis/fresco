package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.protocols.Projection.Coordinate;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

/** Generate a random element in the set {0, ..., p-1} */
public class RandomModP extends CRTComputation<SInt> {

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder, CRTRingDefinition ring,
      CRTNumericContext context) {
    return builder.seq(seq -> seq.numeric().randomElement())
        .pairInPar(
            (seq, r) -> seq.seq(new Projection(r, Coordinate.LEFT)),
            (seq, r) -> seq.seq(new LiftPQProtocol(r)))
        .seq((seq, rPair) -> seq.numeric().add(rPair.getFirst(), rPair.getSecond()));
  }
}
