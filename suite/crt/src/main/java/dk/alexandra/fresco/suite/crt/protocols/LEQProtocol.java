package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

/** Return 1 if x <= y and 0 otherwise. We assume 0 < x,y < p^2 */
public class LEQProtocol extends CRTComputation<SInt> {

  private final DRes<SInt> x, y;

  public LEQProtocol(DRes<SInt> x, DRes<SInt> y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder, CRTRingDefinition ring,
      CRTNumericContext context) {
    return builder.seq(seq -> {
      DRes<SInt> z = seq.numeric().add(ring.getP().pow(2), seq.numeric().sub(y, x));
      return new Truncp(new Truncp(z).buildComputation(seq)).buildComputation(seq);
    });
  }

}
