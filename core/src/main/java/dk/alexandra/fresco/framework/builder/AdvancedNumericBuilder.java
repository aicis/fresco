package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public interface AdvancedNumericBuilder {


  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return An approximation of <i>dividend / divisor</i>.
   */
  Computation<SInt> div(Computation<SInt> dividend, OInt divisor);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return quotient and remainder.
   */
  Computation<SInt> mod(Computation<SInt> dividend, OInt divisor);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return An approximation of <i>dividend / divisor</i>.
   */
  Computation<SInt> div(Computation<SInt> dividend, Computation<SInt> divisor);

  /**
   * Convert an integer to an list of bits, with index 0 being the least
   * significant bit
   *
   * @param in SInt
   * @return the list of bits
   */
  Computation<List<SInt>> toBits(Computation<SInt> in, int maxInputLength);


  Computation<SInt> exp(Computation<SInt> x, Computation<SInt> e, int maxExponentLength);

  Computation<SInt> exp(OInt x, Computation<SInt> e, int maxExponentLength);

  Computation<SInt> exp(Computation<SInt> x, OInt e);

  /**
   * @param input The input.
   * @param maxInputLength An upper bound for <i>log<sub>2</sub>(input)</i>.
   * @return An approximation of the square root of the input.
   */
  Computation<SInt> sqrt(Computation<SInt> input, int maxInputLength);
}
