package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.Util;
import java.math.BigInteger;

public class VerifyProtocol implements Computation<Boolean, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;

  public VerifyProtocol(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<Boolean> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(new MaskAndOpenComputation(value)).seq((seq, chi) -> {
      CRTNumericContext context = (CRTNumericContext) seq.getBasicNumericContext();
      Pair<BigInteger, BigInteger> crt = Util.mapToCRT(chi, context.getP(), context.getQ());
      return DRes.of(crt.getFirst().equals(crt.getSecond().mod(context.getQ())));
    });
  }
}
