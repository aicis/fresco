package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.utils.FixedCondSelect;
import dk.alexandra.fresco.lib.fixed.math.Exponential;
import dk.alexandra.fresco.lib.fixed.math.Logarithm;
import dk.alexandra.fresco.lib.fixed.math.PolynomialEvaluation;
import dk.alexandra.fresco.lib.fixed.math.Reciprocal;
import dk.alexandra.fresco.lib.fixed.math.SquareRoot;
import dk.alexandra.fresco.lib.fixed.math.TwoPower;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultAdvancedFixedNumeric extends AdvancedFixedNumeric {

  protected final ProtocolBuilderNumeric builder;

  public DefaultAdvancedFixedNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SFixed> random() {
    return builder.par(par -> {
      DRes<RandomAdditiveMask> random =
          AdvancedNumeric.using(par).additiveMask(par.getBasicNumericContext().getDefaultFixedPointPrecision());
      return random;
    }).seq((seq, random) -> {
      return () -> new SFixed(random.value);
    });
  }

  @Override
  public DRes<Pair<DRes<SFixed>, DRes<SInt>>> normalize(DRes<SFixed> x) {
    return builder.seq(seq -> {
      DRes<Pair<DRes<SInt>, DRes<SInt>>> normalized = AdvancedNumeric.using(seq)
          .normalize(((SFixed) x.out()).getSInt(),
              seq.getBasicNumericContext().getDefaultFixedPointPrecision() * 2);
      return normalized;
    }).seq((seq, normalized) -> {
      DRes<SFixed> scalingFactor = new SFixed(normalized.getFirst());
      DRes<SInt> scalingPower = seq.numeric().sub(normalized.getSecond(),
          BigInteger.valueOf(seq.getBasicNumericContext().getDefaultFixedPointPrecision()));
      return () -> new Pair<>(scalingFactor, scalingPower);
    });
  }

  @Override
  public DRes<SFixed> condSelect(DRes<SInt> condition, DRes<SFixed> first, DRes<SFixed> second) {
    return builder.seq(seq -> {
      return new FixedCondSelect(condition, (SFixed) first.out(), (SFixed) second.out())
          .buildComputation(seq);
    });
  }

  @Override
  public DRes<SInt> floor(DRes<SFixed> x) {
    return builder.seq(seq -> {
      SFixed xFixed = (SFixed) x.out();
      return AdvancedNumeric.using(seq).rightShift(xFixed.getSInt(),
          seq.getBasicNumericContext().getDefaultFixedPointPrecision());
    });
  }

  @Override
  public DRes<SInt> sign(DRes<SFixed> x) {
    return builder.seq(seq -> {
      SFixed xFixed = x.out();
      return Comparison.using(seq).sign(xFixed.getSInt());
    });
  }

  @Override
  public DRes<SFixed> innerProduct(List<DRes<SFixed>> a, List<DRes<SFixed>> b) {
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

      DRes<SInt> innerProductBeforeTruncation = AdvancedNumeric.using(seq).innerProduct(aFixed, bFixed);

      DRes<SInt> truncated = AdvancedNumeric.using(seq)
          .truncate(innerProductBeforeTruncation, seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      return new SFixed(truncated);
    });
  }

  public DRes<SFixed> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SFixed>> b) {
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
              new BigDecimal(BigInteger.valueOf(2).pow(seq.getBasicNumericContext().getDefaultFixedPointPrecision())))
          .setScale(0, RoundingMode.HALF_UP).toBigIntegerExact()).collect(Collectors.toList());
      List<DRes<SInt>> bFixed =
          b.stream().map(x -> ((SFixed) x.out()).getSInt()).collect(Collectors.toList());

      DRes<SInt> innerProductBeforeTruncation =
          AdvancedNumeric.using(seq).innerProductWithPublicPart(aFixed, bFixed);

      DRes<SInt> truncated = AdvancedNumeric.using(seq)
          .truncate(innerProductBeforeTruncation, seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      return new SFixed(truncated);
    });
  }

  @Override
  public DRes<SFixed> sum(List<DRes<SFixed>> input) {
    return builder.seq(seq -> () -> input)
        .whileLoop((inputs) -> inputs.size() > 1, (seq, inputs) -> seq.par(parallel -> {
          List<DRes<SFixed>> out = new ArrayList<>();
          DRes<SFixed> left = null;
          for (DRes<SFixed> input1 : inputs) {
            if (left == null) {
              left = input1;
            } else {
              out.add(FixedNumeric.using(parallel).add(left, input1));
              left = null;
            }
          }
          if (left != null) {
            out.add(left);
          }
          return () -> out;
        })).seq((r3, currentInput) -> {
          return currentInput.get(0);
        });
  }

  @Override
  public DRes<SFixed> exp(DRes<SFixed> x) {
    return new Exponential(x).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> log(DRes<SFixed> x) {
    return new Logarithm(x).buildComputation(builder);
  }


  @Override
  public DRes<SFixed> reciprocal(DRes<SFixed> x) {
    return new Reciprocal(x).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sqrt(DRes<SFixed> x) {
    return new SquareRoot(x).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> twoPower(DRes<SInt> x) {
    return new TwoPower(x).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> polynomialEvalutation(DRes<SFixed> input, double ... polynomial) {
    return new PolynomialEvaluation(input, polynomial).buildComputation(builder);
  }

}
