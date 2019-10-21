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
import dk.alexandra.fresco.lib.real.fixed.utils.Truncate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

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

  @Override
  public DRes<SReal> innerProduct(List<DRes<SReal>> a, List<DRes<SReal>> b) {
    return builder.seq(seq -> {

      /*
       * Exploit that we are working with fixed point numbers to avoid truncation for each
       * multiplication. Instead one truncation is done at the end. Note that this could result in
       * an overflow if the sum of the products (un-truncated) exceeds the max bit length.
       */

      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      List<DRes<SInt>> aFixed =
          a.stream().map(x -> ((SFixed) x.out()).getSInt()).collect(Collectors.toList());
      List<DRes<SInt>> bFixed =
          b.stream().map(x -> ((SFixed) x.out()).getSInt()).collect(Collectors.toList());

      DRes<SInt> innerProductBeforeTruncation = seq.advancedNumeric().innerProduct(aFixed, bFixed);

      DRes<SInt> truncated =
          new Truncate(innerProductBeforeTruncation, seq.getRealNumericContext().getPrecision())
              .buildComputation(seq);
      return new SFixed(truncated);
    });
  }

  public DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b) {
    return builder.seq(seq -> {

      /*
       * Exploit that we are working with fixed point numbers to avoid truncation for each
       * multiplication. Instead one truncation is done at the end. Note that this could result in
       * an overflow if the sum of the products (un-truncated) exceeds the max bit length.
       */

      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      List<BigInteger> aFixed = a.stream().map(x -> x
          .multiply(
              new BigDecimal(BigInteger.valueOf(2).pow(seq.getRealNumericContext().getPrecision())))
          .setScale(0, RoundingMode.HALF_UP).toBigIntegerExact()).collect(Collectors.toList());
      List<DRes<SInt>> bFixed =
          b.stream().map(x -> ((SFixed) x.out()).getSInt()).collect(Collectors.toList());

      DRes<SInt> innerProductBeforeTruncation =
          seq.advancedNumeric().innerProductWithPublicPart(aFixed, bFixed);

      DRes<SInt> truncated =
          new Truncate(innerProductBeforeTruncation, seq.getRealNumericContext().getPrecision())
              .buildComputation(seq);
      return new SFixed(truncated);
    });
  }
}
