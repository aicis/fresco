package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import java.math.BigInteger;

public class DummyMixedAddProtocol extends
    CRTComputation<BigInteger> {

  private final CRTSInt value;

  public DummyMixedAddProtocol(DRes<SInt> xp, DRes<SInt> xq) {
    this.value = new CRTSInt(xp, xq);
  }

  public DummyMixedAddProtocol(DRes<SInt> value) {
    this.value = (CRTSInt) value.out();
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric builder, CRTRingDefinition ring,
      CRTNumericContext context) {
    return builder.seq(seq -> seq.numeric().open(value)).seq((seq, open) -> {
      Pair<BigInteger, BigInteger> crt = ring.mapToCRT(open);
      return DRes.of(crt.getFirst().add(crt.getSecond()));
    });
  }

}
