package dk.alexandra.fresco.suite.crt.comparison;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.DefaultComparison;
import dk.alexandra.fresco.suite.crt.protocols.LEQProtocol;

public class CRTComparison extends DefaultComparison {

  public CRTComparison(
      ProtocolBuilderNumeric builder) {
    super(builder);
  }

  @Override
  public DRes<SInt> compareLEQLong(DRes<SInt> x, DRes<SInt> y) {
    return compareLEQ(x, y);
  }

  @Override
  public DRes<SInt> compareLEQ(DRes<SInt> x, DRes<SInt> y) {
    return builder.seq(new LEQProtocol(x, y));
  }

}
