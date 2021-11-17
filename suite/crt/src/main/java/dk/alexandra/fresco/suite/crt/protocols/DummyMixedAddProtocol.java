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

public class DummyMixedAddProtocol implements
    Computation<BigInteger, ProtocolBuilderNumeric> {

  private final CRTSInt value;

  public DummyMixedAddProtocol(DRes<SInt> xp, DRes<SInt> xq) {
    this.value = new CRTSInt(xp, xq);
  }

  public DummyMixedAddProtocol(DRes<SInt> value) {
    this.value = (CRTSInt) value.out();
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric builder) {
    // TODO: Currently done in clear text
    return builder.seq(seq -> seq.numeric().open(value)).seq((seq, open) -> {
      CRTRingDefinition ring = (CRTRingDefinition) seq.getBasicNumericContext()
          .getFieldDefinition();
      Pair<BigInteger, BigInteger> crt = Util.mapToCRT(open, ring.getP(), ring.getQ());
      return DRes.of(crt.getFirst().add(crt.getSecond()));
    });
  }
}
