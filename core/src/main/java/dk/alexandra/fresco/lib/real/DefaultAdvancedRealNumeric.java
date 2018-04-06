package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DefaultAdvancedRealNumeric implements AdvancedRealNumeric {

  protected final ProtocolBuilderNumeric builder;

  protected DefaultAdvancedRealNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SReal> sum(List<DRes<SReal>> input) {
    return builder.seq(seq -> () -> input)
        .whileLoop((inputs) -> inputs.size() > 1, (seq, inputs) -> seq.par(parallel -> {
          List<DRes<SReal>> out = new ArrayList<>();
          DRes<SReal> left = null;
          for (DRes<SReal> input1 : inputs) {
            if (left == null) {
              left = input1;
            } else {
              out.add(parallel.realNumeric().add(left, input1));
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

      List<DRes<SReal>> products = new ArrayList<>(a.size());
      for (int i = 0; i < a.size(); i++) {
        products.add(par.realNumeric().mult(a.get(i), b.get(i)));
      }

      return () -> products;
    }).seq((seq, list) -> {
      return seq.realAdvanced().sum(list);
    });
  }

  @Override
  public DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b) {
    return builder.par(r1 -> {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      List<DRes<SReal>> products = new ArrayList<>(a.size());
      for (int i = 0; i < a.size(); i++) {
        products.add(r1.realNumeric().mult(a.get(i), b.get(i)));
      }

      return () -> products;
    }).seq((seq, list) -> {
      return seq.realAdvanced().sum(list);
    });
  }

  @Override
  public DRes<SReal> exp(DRes<SReal> x) {

    int numberOfTerms = 16;
    return builder.seq(seq -> {
      List<DRes<SReal>> powers = new ArrayList<>(numberOfTerms - 1);

      powers.add(x);
      DRes<SReal> pow = x;
      for (int i = 1; i < numberOfTerms - 1; i++) {
        pow = seq.realNumeric().mult(pow, x);
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
       * calculate many more terms, since the error after n terms is approx x^{n+1}/(n+1)!), and
       * this would cause the function to be very inefficient. Maybe we should allow the app
       * developer to specify an upper bound on the input?
       */

      BigInteger n = BigInteger.ONE;
      List<DRes<SReal>> terms = new ArrayList<>(numberOfTerms);
      for (int i = numberOfTerms - 1; i >= 1; i--) {
        terms.add(par.realNumeric().mult(new BigDecimal(n), powers.get(i - 1)));
        n = n.multiply(BigInteger.valueOf(i));
      }
      final BigDecimal divisor = new BigDecimal(n);
      terms.add(par.realNumeric().known(divisor));
      return () -> new Pair<>(terms, divisor);
    }).seq((seq, termsAndDivisor) -> {
      return seq.realNumeric().div(seq.realAdvanced().sum(termsAndDivisor.getFirst()),
          termsAndDivisor.getSecond());
    });
  }

  @Override
  public DRes<SReal> log(DRes<SReal> x) {
    int numberOfTerms = 16;

    /*
     * The logarithm is calculated as the Taylor expansion based on the identity log(x) = 2
     * artanh((x-1)/(x+1)). See https://en.wikipedia.org/wiki/Logarithm#Power_series.
     * 
     * It works okay for small inputs (< 40), but the convergence rate is too slow for larger inputs
     * to get precise results.
     */

    return builder.seq(seq -> {
      DRes<SReal> y = seq.realNumeric().div(seq.realNumeric().sub(x, BigDecimal.ONE),
          seq.realNumeric().add(BigDecimal.ONE, x));
      DRes<SReal> ySquared = seq.realNumeric().mult(y, y);

      List<DRes<SReal>> powers = new ArrayList<>(numberOfTerms);
      powers.add(y);
      DRes<SReal> currentPower = y;
      for (int i = 1; i < numberOfTerms; i++) {
        currentPower = seq.realNumeric().mult(currentPower, ySquared);
        powers.add(currentPower);
      }
      return () -> powers;
    }).par((par, powers) -> {
      List<DRes<SReal>> terms = powers.stream()
          .map(
              e -> par.realNumeric().mult(new BigDecimal(1.0 / (2 * powers.indexOf(e) + 1)), e))
          .collect(Collectors.toList());
      return () -> terms;
    }).seq((seq, terms) -> {
      return seq.realNumeric().mult(BigDecimal.valueOf(2.0), seq.realAdvanced().sum(terms));
    });
  }

}
