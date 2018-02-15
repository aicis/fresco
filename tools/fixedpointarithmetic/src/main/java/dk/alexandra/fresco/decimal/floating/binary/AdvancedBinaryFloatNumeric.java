package dk.alexandra.fresco.decimal.floating.binary;

import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class AdvancedBinaryFloatNumeric extends DefaultAdvancedRealNumeric {

  private final ProtocolBuilderNumeric builder;

  public AdvancedBinaryFloatNumeric(ProtocolBuilderNumeric builder) {
    super(builder, scope -> new BinaryFloatNumeric(scope));
    this.builder = builder;
  }

  @Override
  public DRes<SInt> leq(DRes<SReal> x, DRes<SReal> y) {
    return builder.seq(seq -> {
      int scale = Math.max(((SBinaryFloat) x.out()).getScale(), ((SBinaryFloat) y.out()).getScale());
      DRes<SInt> xint = ((SBinaryFloat) x.out()).getSInt();
      if (((SBinaryFloat) x.out()).getScale() < scale) {
        xint = seq.numeric().mult(BigInteger.valueOf(2).pow(scale - ((SBinaryFloat) x.out()).getScale()), xint);
      }
      DRes<SInt> yint = ((SBinaryFloat) y.out()).getSInt();
      if (((SBinaryFloat) y.out()).getScale() < scale) {
        yint = seq.numeric().mult(BigInteger.valueOf(2).pow(scale - ((SBinaryFloat) y.out()).getScale()), yint);
      }
      return seq.comparison().compareLEQ(xint, yint);
    });
  }

}
