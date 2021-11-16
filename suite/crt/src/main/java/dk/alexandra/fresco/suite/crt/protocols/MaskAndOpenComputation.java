package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class MaskAndOpenComputation implements Computation<BigInteger, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;

  public MaskAndOpenComputation(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> seq.append(new CorrelatedNoiseProtocol<>()))
        .seq((seq, r) -> seq.numeric().add(value, r)).seq((seq, x) -> seq.numeric().open(x));
  }
}
