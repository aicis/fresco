package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigInteger;

public class AdvancedFixedNumeric extends DefaultAdvancedRealNumeric {

  public AdvancedFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder);
  }

  @Override
  public DRes<SReal> sqrt(DRes<SReal> x) {
    return builder.seq(seq -> {
      SFixed cast = (SFixed) x.out();
      DRes<SInt> underlyingInt = cast.getSInt();

      DRes<SInt> intResult =
          seq.advancedNumeric().sqrt(underlyingInt, seq.getBasicNumericContext().getMaxBitLength());

      DRes<SInt> truncated = seq.numeric().mult(
          BigInteger.ONE.shiftLeft(seq.getRealNumericContext().getPrecision() / 2), intResult);

      return new SFixed(truncated);
    });

  }

  @Override
  public DRes<SReal> random() {
    return builder.seq(seq -> {
      DRes<RandomAdditiveMask> random =
          seq.advancedNumeric().additiveMask(seq.getRealNumericContext().getPrecision());
      return random;
    }).seq((seq, random) -> {
      return () -> new SFixed(random.random);
    });
  }
}
