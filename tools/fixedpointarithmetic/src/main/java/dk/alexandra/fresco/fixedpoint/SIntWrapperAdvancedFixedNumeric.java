package dk.alexandra.fresco.fixedpoint;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

public class SIntWrapperAdvancedFixedNumeric implements AdvancedFixedNumeric {

  private ProtocolBuilderNumeric builder;
  private int precision;

  public SIntWrapperAdvancedFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
  }

  @Override
  public DRes<SFixed> exp(DRes<SFixed> x) {
    int terms = 16; 
    BigDecimal[] coefficients = new BigDecimal[terms];
    BigInteger n = BigInteger.ONE;
    coefficients[terms - 1] = new BigDecimal(n);
    for (int i = terms - 1; i >= 1; i--) {
      n = n.multiply(BigInteger.valueOf(i));
      coefficients[i - 1] = new BigDecimal(n);
    }
    
    return builder.seq(seq -> {
      /*
       * We approximate the exponential function by calculating the first terms
       * of the Taylor expansion. By letting all terms in the series have common
       * denominator, we only need to do one division.
       * 
       * TODO: In the current implementation we compute 16 terms, which seems to
       * give decent percision for small inputs. If we want full precision for
       * all possible inputs, we need to calculate many more terms, since the
       * error after n terms is approx 1/(n+1)! x^{n+1}), and this would cause
       * the function to be very inefficient. Maybe we should allow the app
       * developer to specify an upper bound on the input?
       * 
       * TODO: The multiplications can be done in parallel if all powers are
       * calculated first.
       */
      
      FixedNumeric fixed = new DefaultFixedNumeric(seq, precision);
      DRes<SFixed> sum = fixed.numeric().known(coefficients[0]);
      DRes<SFixed> pow = fixed.numeric().known(BigDecimal.ONE);
      for (int i = 1; i < terms; i++) {
        pow = fixed.numeric().mult(pow, x);
        sum = fixed.numeric().add(sum, fixed.numeric().mult(coefficients[i], pow));
      }
      return fixed.numeric().div(sum, coefficients[0]);            
    });
  }
    
  @Override
  public DRes<SFixed> random() {
    return builder.seq(new FixedPointRandom(precision));
  }

  @Override
  public DRes<SInt> leq(DRes<SFixed> x, DRes<SFixed> y) {
    return builder.seq(seq -> {
      return seq.comparison().compareLEQ(((SFixedSIntWrapper) x.out()).getSInt(),
          ((SFixedSIntWrapper) y.out()).getSInt());
    });
  }

  @Override
  public DRes<SFixed> sum(List<DRes<SFixed>> terms) {
    return builder.seq(seq -> {
      List<DRes<SInt>> ints = new ArrayList<>();
      for (DRes<SFixed> term : terms) {
        ints.add(((SFixedSIntWrapper) term.out()).getSInt());
      }
      return new SFixedSIntWrapper(builder.advancedNumeric().sum(ints));      
    });
  }
}
