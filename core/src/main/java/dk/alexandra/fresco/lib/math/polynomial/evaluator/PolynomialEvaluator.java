package dk.alexandra.fresco.lib.math.polynomial.evaluator;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.polynomial.Polynomial;

/**
 * Evaluates a polynomial using Horner's method.
 */
public class PolynomialEvaluator implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> valueX;
  private final Polynomial polynomial;

  public PolynomialEvaluator(DRes<SInt> x, Polynomial p) {
    this.valueX = x;
    this.polynomial = p;
  }


  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    int degree = polynomial.getMaxDegree();

    /*
     * We use Horner's method, p(x) = (( ... ((p_{n-1} x + p_{n-2})x +
     * p_{n-3}) ... )x + a_1)x + a_0
     */
    DRes<SInt> tmp = polynomial.getCoefficient(degree - 1);

    for (int i = degree - 2; i >= 0; i--) {
      tmp = builder.numeric().mult(tmp, valueX);
      tmp = builder.numeric().add(tmp, polynomial.getCoefficient(i));
    }

    return tmp;
  }

}
