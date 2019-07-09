package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.utils.FixedCondSelect;
import dk.alexandra.fresco.lib.real.fixed.utils.NormalizeSInt;
import java.math.BigInteger;

public class AdvancedFixedNumeric extends DefaultAdvancedRealNumeric {

  public AdvancedFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder);
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

  @Override
  public DRes<Pair<DRes<SReal>, DRes<SInt>>> normalize(DRes<SReal> x) {
    return builder.seq(seq -> {
      DRes<Pair<DRes<SInt>, DRes<SInt>>> normalized =
          new NormalizeSInt(((SFixed) x.out()).getSInt(),
              seq.getRealNumericContext().getPrecision() * 2).buildComputation(seq);
      return normalized;
    }).seq((seq, normalized) -> {
      DRes<SReal> scalingFactor = new SFixed(normalized.getFirst());
      DRes<SInt> scalingPower = seq.numeric().sub(normalized.getSecond(),
          BigInteger.valueOf(seq.getRealNumericContext().getPrecision()));
      return () -> new Pair<>(scalingFactor, scalingPower);
    });
  }

  @Override
  public DRes<SReal> condSelect(DRes<SInt> condition, DRes<SReal> first, DRes<SReal> second) {
    return builder.seq(seq -> {
      return new FixedCondSelect(condition, (SFixed) first.out(), (SFixed) second.out())
          .buildComputation(seq);
    });
  }

  @Override
  public DRes<SInt> floor(DRes<SReal> x) {
    return builder.seq(seq -> {
      SFixed xFixed = (SFixed) x.out();
      return seq.advancedNumeric().rightShift(xFixed.getSInt(),
          seq.getRealNumericContext().getPrecision());
    });
  }

  @Override
  public DRes<SInt> sign(DRes<SReal> x) {
    return builder.seq(seq -> {
      SFixed xFixed = (SFixed) x.out();
      return seq.comparison().sign(xFixed.getSInt());
    });
  }
}
