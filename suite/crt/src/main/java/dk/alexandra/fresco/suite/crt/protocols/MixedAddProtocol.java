package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import java.math.BigInteger;

public class MixedAddProtocol implements
    Computation<BigInteger, ProtocolBuilderNumeric> {

  private final CRTSInt value;

  public MixedAddProtocol(DRes<SInt> xp, DRes<SInt> xq) {
    this.value = new CRTSInt(xp, xq);
  }

  public MixedAddProtocol(CRTSInt value) {
    this.value = value;
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric builder) {
    // TODO: Currently done in clear text -- how do we avoid overflow in the shares?
    return builder.seq(seq -> seq.numeric().open(value)).seq((seq, open) -> {
      CRTRingDefinition ring = (CRTRingDefinition) seq.getBasicNumericContext()
          .getFieldDefinition();
      Pair<BigInteger, BigInteger> crt = Util.mapToCRT(open, ring.getP(), ring.getQ());
      return DRes.of(crt.getFirst().add(crt.getSecond()));
    });
  }
}
