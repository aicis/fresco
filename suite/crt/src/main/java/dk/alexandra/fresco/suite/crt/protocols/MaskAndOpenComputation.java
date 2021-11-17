package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/** Add correlated noise to a number and open it */
public class MaskAndOpenComputation implements Computation<BigInteger, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;

  public MaskAndOpenComputation(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> seq.append(new CorrelatedNoiseProtocol<>()))
        .seq((seq, r) -> seq.numeric().open(seq.numeric().add(value, r)));
  }
}
