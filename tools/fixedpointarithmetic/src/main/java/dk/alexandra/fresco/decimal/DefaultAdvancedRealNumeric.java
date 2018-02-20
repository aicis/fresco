package dk.alexandra.fresco.decimal;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultAdvancedRealNumeric implements AdvancedRealNumeric {

  private final ProtocolBuilderNumeric builder;
  private final RealNumericProvider provider;

  private static final int DEFAULT_RANDOM_BITS = 32;

  protected DefaultAdvancedRealNumeric(ProtocolBuilderNumeric builder,
      RealNumericProvider provider) {
    this.builder = builder;
    this.provider = provider;
  }

  @Override
  public DRes<SReal> sum(List<DRes<SReal>> input) {
    return builder.seq(seq -> () -> input)
        .whileLoop((inputs) -> inputs.size() > 1, (seq, inputs) -> seq.par(parallel -> {
          List<DRes<SReal>> out = new ArrayList<>();
          RealNumeric numeric = provider.apply(parallel);
          DRes<SReal> left = null;
          for (DRes<SReal> input1 : inputs) {
            if (left == null) {
              left = input1;
            } else {
              out.add(numeric.numeric().add(left, input1));
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
  public DRes<SReal> innerProduct(List<DRes<SReal>> a, List<DRes<SReal>> b) {
    return builder.par(par -> {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      RealNumeric fixed = provider.apply(par);
      List<DRes<SReal>> products = new ArrayList<>(a.size());
      for (int i = 0; i < a.size(); i++) {
        products.add(fixed.numeric().mult(a.get(i), b.get(i)));
      }

      return () -> products;
    }).seq((seq, list) -> {
      RealNumeric fixed = provider.apply(seq);
      return fixed.advanced().sum(list);
    });
  }

  @Override
  public DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b) {
    return builder.par(r1 -> {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      RealNumeric fixed = provider.apply(r1);
      List<DRes<SReal>> products = new ArrayList<>(a.size());
      for (int i = 0; i < a.size(); i++) {
        products.add(fixed.numeric().mult(a.get(i), b.get(i)));
      }

      return () -> products;
    }).seq((seq, list) -> {
      RealNumeric fixed = provider.apply(seq);
      return fixed.advanced().sum(list);
    });
  }

  @Override
  public DRes<SReal> exp(DRes<SReal> x) {

    int numberOfTerms = 16;
    return builder.seq(seq -> {
      List<DRes<SReal>> powers = new ArrayList<>(numberOfTerms - 1);

      powers.add(x);
      DRes<SReal> pow = x;
      RealNumeric fixed = provider.apply(seq);
      for (int i = 1; i < numberOfTerms - 1; i++) {
        pow = fixed.numeric().mult(pow, x);
        powers.add(pow);
      }
      return () -> powers;
    }).par((par, powers) -> {
      
      /*
       * We approximate the exponential function by calculating the first terms of the Taylor
       * expansion. By letting all terms in the series have common denominator, we only need to do
       * one division.
       * 
       * TODO: In the current implementation we compute 16 terms, which seems to give decent
       * percision for small inputs. If we want full precision for all possible inputs, we need to
       * calculate many more terms, since the error after n terms is approx 1/(n+1)! x^{n+1}), and
       * this would cause the function to be very inefficient. Maybe we should allow the app
       * developer to specify an upper bound on the input?
       */

      BigInteger n = BigInteger.ONE;
      List<DRes<SReal>> terms = new ArrayList<>(numberOfTerms);
      RealNumeric fixed = provider.apply(par);
      for (int i = numberOfTerms - 1; i >= 1; i--) {
        terms.add(fixed.numeric().mult(new BigDecimal(n), powers.get(i - 1)));
        n = n.multiply(BigInteger.valueOf(i));
      }
      final BigDecimal divisor = new BigDecimal(n);
      terms.add(fixed.numeric().known(divisor));
      return () -> new Pair<>(terms, divisor);
    }).seq((seq, termsAndDivisor) -> {
      RealNumeric fixed = provider.apply(seq);
      return fixed.numeric().div(fixed.advanced().sum(termsAndDivisor.getFirst()),
          termsAndDivisor.getSecond());
    });
  }

  @Override
  public DRes<SReal> random() {
    return builder.seq(seq -> {
      DRes<RandomAdditiveMask> random = seq.advancedNumeric().additiveMask(DEFAULT_RANDOM_BITS);
      return random;
    }).seq((seq, random) -> {
      RealNumeric numeric = provider.apply(seq);
      DRes<SInt> randomInteger = random.random;
      BigInteger divisor = BigInteger.ONE.shiftLeft(DEFAULT_RANDOM_BITS);
      return numeric.numeric().div(numeric.numeric().fromSInt(randomInteger),
          new BigDecimal(divisor));
    });
  }

}
