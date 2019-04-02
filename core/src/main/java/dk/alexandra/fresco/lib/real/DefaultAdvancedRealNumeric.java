package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultAdvancedRealNumeric implements AdvancedRealNumeric {

  protected final ProtocolBuilderNumeric builder;
  private static final BigDecimal TWO = BigDecimal.valueOf(2);

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

  /**
   * We use a fast converging series for the natural logarithm. The number of terms, 12, is a bit
   * arbitrary but gives decent precision for small inputs.
   *
   * The approximation is based on the series ln(x) = 2t * \sum_{k=0}^\infty 1 / (2k + 1) t^{2k} for
   * t = (x-1) / (x+1).
   * 
   * The approximation is best for small inputs (the error is bounded by 0.0014 for 0.1 < x < 10 and
   * by -0.24 for x < 50, but for larger inputs, the error term is rather big but can be
   * approximated (and compensated for) using one of the polynomials (constant term first, optimised
   * for x < 1000):
   * 
   * Linear [-0.02771369259544744, -0.0030294386027190858]
   * 
   * Quadratic [0.05195678095575384, -0.005443079020109276, 3.0571652211820656E-6]
   * 
   * Cubic [0.08080174700015502, -0.006644457297258054, 6.849009473279065E-6,
   * -2.8055166996698435E-9]
   * 
   * Quartic [0.07313345035356501, -0.0062624966485768045, 4.556408175523062E-6,
   * 1.3107035589962076E-9, -2.243237315197184E-12]
   * 
   * @param x The input value.
   * @returns A deferred result approximating the natural logarithm of x.
   */
  @Override
  public DRes<SReal> log(DRes<SReal> x) {

    /*
     * We use a fast converging series for the natural logarithm. The number of terms, 12, is a bit
     * arbitrary but gives decent precision for small inputs.
     *
     * The approximation is based on the series ln(x) = 2t * \sum_{k=0}^\infty 1 / (2k + 1) t^{2k}
     * for t = (x-1) / (x+1).
     */
    int iterations = 12;
    return builder.seq(seq -> {

      DRes<SReal> t = seq.realNumeric().div(seq.realNumeric().sub(x, BigDecimal.ONE),
          seq.realNumeric().add(BigDecimal.ONE, x));
      DRes<SReal> tSquared = seq.realNumeric().mult(t, t);

      List<DRes<SReal>> powers = new ArrayList<>();

      DRes<SReal> tp = tSquared;
      powers.add(tp);
      for (int i = 0; i < iterations - 2; i++) {
        tp = seq.realNumeric().mult(tp, tSquared);
        powers.add(tp);
      }
      return () -> new Pair<>(powers, t);
    }).par((par, v) -> {

      DRes<SReal> s = par.realNumeric().mult(TWO, v.getSecond());

      List<DRes<SReal>> terms = new ArrayList<>();
      terms.add(par.realNumeric().known(BigDecimal.ONE));
      for (int i = 1; i < iterations; i++) {
        terms.add(par.realNumeric().div(v.getFirst().get(i - 1), BigDecimal.valueOf(2 * i + 1)));
      }
      return () -> new Pair<>(terms, s);
    }).seq((seq, v) -> seq.realNumeric().mult(v.getSecond(), seq.realAdvanced().sum(v.getFirst())));

  }

}
