package dk.alexandra.fresco.fixedpoint;

import java.math.BigDecimal;
import java.math.BigInteger;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class SIntWrapperAdvancedFixedNumeric implements AdvancedFixedNumeric {

  private ProtocolBuilderNumeric builder;
  private int precision;

  public SIntWrapperAdvancedFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
  }

  /**
   * Get a FixedNumeric based on the given builder.
   * 
   * @param builder
   * @return
   */
  private BasicFixedNumeric getFixedNumeric(ProtocolBuilderNumeric builder) {
    return new SIntWrapperFixedNumeric(builder, precision);
  }

  @Override
  public DRes<SFixed> exp(DRes<SFixed> x) {

    /*
     * We approximate the exponential function by calculating the first terms of
     * the Taylor expansion. By letting all terms in the series have common
     * denominator, we only need to do one division.
     * 
     * TODO: In the current implementation we compute 16 terms, which seems to
     * give decent percision for small inputs. If we want full precision for all
     * possible inputs, we need to calculate many more terms, since the error
     * after n terms is approx 1/(n+1)! x^{n+1}), and this would cause the
     * function to be very inefficient. Maybe we should allow the app developer
     * to specify an upper bound on the input?
     */

    int terms = 16; 
    BigDecimal[] coefficients = new BigDecimal[terms];
    BigInteger n = BigInteger.ONE;
    coefficients[terms - 1] = new BigDecimal(n);
    for (int i = terms - 1; i >= 1; i--) {
      n = n.multiply(BigInteger.valueOf(i));
      coefficients[i - 1] = new BigDecimal(n);
    }

    BasicFixedNumeric fixed = getFixedNumeric(builder);
    DRes<SFixed> sum = fixed.known(coefficients[0]);
    DRes<SFixed> pow = fixed.known(BigDecimal.ONE);
    for (int i = 1; i < terms; i++) {
      pow = fixed.mult(pow, x);
      sum = fixed.add(sum, fixed.mult(coefficients[i], pow));
    }
    return fixed.div(sum, coefficients[0]);
  }
    
  @Override
  public DRes<SFixed> random() {
    return builder.seq(new FixedPointRandom(precision));
  }
}
