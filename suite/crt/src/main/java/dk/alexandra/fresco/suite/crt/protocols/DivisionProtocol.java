package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import java.math.BigInteger;

public class DivisionProtocol extends CRTComputation<SInt> {

  private final DRes<SInt> x;
  private final BigInteger d;

  public DivisionProtocol(DRes<SInt> x, BigInteger d) {
    this.x = x;
    this.d = d;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder, CRTRingDefinition ring,
      CRTNumericContext context) {
    BigInteger l = ring.getP().pow(2).divide(d).add(BigInteger.ONE);
    return builder.seq(seq -> new Truncp(new Truncp(seq.numeric().mult(l, x)).buildComputation(seq)).buildComputation(seq));
  }

}
