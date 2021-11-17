package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import java.math.BigInteger;

public class DivisionProtocol implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> x;
  private final BigInteger d;

  public DivisionProtocol(DRes<SInt> x, BigInteger d) {
    this.x = x;
    this.d = d;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      BigInteger p = ((CRTRingDefinition) seq.getBasicNumericContext().getFieldDefinition()).getP();
      BigInteger l = p.pow(2).divide(d).add(BigInteger.ONE);
      return new Truncp(new Truncp(seq.numeric().mult(l, x)).buildComputation(seq)).buildComputation(seq);
    });
  }
}
