package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;

public class AdvancedFixedNumeric extends DefaultAdvancedRealNumeric {

  public AdvancedFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder);
  }

  @Override
  public DRes<SReal> sqrt(DRes<SReal> x) {
    return builder.seq(seq -> {
      SFixed cast = (SFixed) x.out();
      DRes<SInt> underlyingInt = cast.getSInt();
      int scale = cast.getPrecision();
      DRes<SInt> intResult =
          seq.advancedNumeric().sqrt(underlyingInt, seq.getBasicNumericContext().getMaxBitLength());

      int newScale = Math.floorDiv(scale, 2);
      DRes<SReal> result = new SFixed(intResult, newScale);

      int scaleResidue = Math.floorMod(scale, 2);
      if (scaleResidue == 1) {
        result = seq.realNumeric().mult(BigDecimal.valueOf(1.0 / Math.sqrt(2.0)), result);
      }
      return result;
    });

  }

  @Override
  public DRes<SReal> random(int bits) {
    return builder.seq(seq -> {
      DRes<RandomAdditiveMask> random = seq.advancedNumeric().additiveMask(bits);
      return random;
    }).seq((seq, random) -> {
      return () -> new SFixed(random.random, bits);
    });
  }
}
