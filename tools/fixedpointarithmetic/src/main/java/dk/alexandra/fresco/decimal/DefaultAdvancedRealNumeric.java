package dk.alexandra.fresco.decimal;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultAdvancedRealNumeric implements AdvancedRealNumeric {

  private final ProtocolBuilderNumeric builder;
  private final RealNumericProvider provider;

  private static final int DEFAULT_RANDOM_PRECISION = 10;

  protected DefaultAdvancedRealNumeric(ProtocolBuilderNumeric builder,
      RealNumericProvider provider) {
    this.builder = builder;
    this.provider = provider;
  }

  @Override
  public abstract DRes<SInt> leq(DRes<SReal> x, DRes<SReal> y);

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
      RealNumeric fixed = provider.apply(r1);
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

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

    return builder.seq(seq -> {
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
       * 
       * TODO: The multiplications can be done in parallel if all powers are calculated first.
       */

      int terms = 16;
      BigDecimal[] coefficients = new BigDecimal[terms];
      BigInteger n = BigInteger.ONE;
      coefficients[terms - 1] = new BigDecimal(n);
      for (int i = terms - 1; i >= 1; i--) {
        n = n.multiply(BigInteger.valueOf(i));
        coefficients[i - 1] = new BigDecimal(n);
      }

      RealNumeric fixed = provider.apply(seq);
      DRes<SReal> pow = x;
      DRes<SReal> sum = fixed.numeric().known(coefficients[0]);
      for (int i = 1; i < terms; i++) {
        sum = fixed.numeric().add(sum, fixed.numeric().mult(coefficients[i], pow));
        if (i <= terms - 2) {
          pow = fixed.numeric().mult(pow, x);
        }
      }
      return fixed.numeric().div(sum, coefficients[0]);
    });
  }

  @Override
  public DRes<SReal> random() {
    int scaleSize =
        (int) Math.ceil((Math.log(Math.pow(10, DEFAULT_RANDOM_PRECISION)) / (Math.log(2))));

    return builder.seq(seq -> {
      DRes<RandomAdditiveMask> random = seq.advancedNumeric().additiveMask(scaleSize);
      return random;
    }).seq((seq, random) -> {

      RealNumeric numeric = provider.apply(seq);

      DRes<SInt> rand = random.random;
      BigInteger divi = BigInteger.valueOf(2).pow(scaleSize);
      DRes<SInt> r2 = seq.numeric().mult(BigInteger.TEN.pow(DEFAULT_RANDOM_PRECISION), rand);
      DRes<SInt> result = seq.advancedNumeric().div(r2, divi);

      return numeric.numeric().div(numeric.numeric().fromSInt(result), new BigDecimal(divi));
    });
  }

}
