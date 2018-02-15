package dk.alexandra.fresco.decimal.floating;

import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class AdvancedFloatNumeric extends DefaultAdvancedRealNumeric {

  private final ProtocolBuilderNumeric builder;

  public AdvancedFloatNumeric(ProtocolBuilderNumeric builder) {
    super(builder, scope -> new FloatNumeric(scope));
    this.builder = builder;
  }

  @Override
  public DRes<SInt> leq(DRes<SReal> x, DRes<SReal> y) {
    return builder.seq(seq -> {
      int scale = Math.max(((SFloat) x.out()).getScale(), ((SFloat) y.out()).getScale());
      DRes<SInt> xint = ((SFloat) x.out()).getSInt();
      if (((SFloat) x.out()).getScale() < scale) {
        xint = seq.numeric().mult(BigInteger.TEN.pow(scale - ((SFloat) x.out()).getScale()), xint);
      }
      DRes<SInt> yint = ((SFloat) y.out()).getSInt();
      if (((SFloat) y.out()).getScale() < scale) {
        yint = seq.numeric().mult(BigInteger.TEN.pow(scale - ((SFloat) y.out()).getScale()), yint);
      }
      return seq.comparison().compareLEQ(xint, yint);
    });
  }

}
