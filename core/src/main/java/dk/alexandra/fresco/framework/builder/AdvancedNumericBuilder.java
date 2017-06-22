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
  public Computation<SInt> div(Computation<SInt> dividend, OInt divisor);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return quotient and remainder.
   */
  public Computation<SInt> remainder(Computation<SInt> dividend, OInt divisor);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return An approximation of <i>dividend / divisor</i>.
   */
  public Computation<SInt> div(Computation<SInt> dividend, Computation<SInt> divisor);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @param precision The protocol gives a guaranteed lower bound for the number of correct bits of
   * the approximation.
   * @return An approximation of <i>dividend / divisor</i>.
   */
  public Computation<SInt> div(Computation<SInt> dividend, Computation<SInt> divisor,
      OInt precision);

  /**
   * Convert an integer to an list of bits, with index 0 being the least
   * significant bit
   *
   * @param in SInt
   * @return the list of bits
   */
  public Computation<List<SInt>> toBits(SInt in, int maxInputLength);

}
