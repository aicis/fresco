package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
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
  Computation<SInt> div(Computation<SInt> dividend, BigInteger divisor);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return quotient and remainder.
   */
  Computation<SInt> mod(Computation<SInt> dividend, BigInteger divisor);

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

  Computation<SInt> exp(BigInteger x, Computation<SInt> e, int maxExponentLength);

  Computation<SInt> exp(Computation<SInt> x, BigInteger e);

  /**
   * @param input The input.
   * @param maxInputLength An upper bound for <i>log<sub>2</sub>(input)</i>.
   * @return An approximation of the square root of the input.
   */
  Computation<SInt> sqrt(Computation<SInt> input, int maxInputLength);

  /**
   * Calculating the natural logarithm of a given input.
   *
   * @param input The input.
   * @param maxInputLength An upper bound for the bit length of the input.
   * @return The natural logarithm of the input.
   */
  Computation<SInt> log(Computation<SInt> input, int maxInputLength);

  Computation<SInt> dot(List<Computation<SInt>> aVector, List<Computation<SInt>> bVector);

  Computation<SInt> openDot(List<BigInteger> aVector, List<Computation<SInt>> bVector);

  Computation<RandomAdditiveMask> additiveMask(int noOfBits);

  /**
   * @param input input
   * @return result input >> 1
   */
  Computation<SInt> rightShift(Computation<SInt> input);

  /**
   * @param input input
   * @return result: input >> 1<br> remainder: The <code>shifts</code> least significant bits of the
   * input with the least significant having index 0.
   */
  Computation<RightShiftResult> rightShiftWithRemainder(Computation<SInt> input);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return result input >> shifts
   */
  Computation<SInt> rightShift(Computation<SInt> input, int shifts);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return result: input >> shifts<br> remainder: The <code>shifts</code> least significant bits
   * of the input with the least significant having index 0.
   */
  Computation<RightShiftResult> rightShiftWithRemainder(Computation<SInt> input,
      int shifts);

  Computation<SInt> bitLength(Computation<SInt> input, int maxBitLength);

  Computation<SInt> invert(Computation<SInt> x);

  /**
   * result input >> 1
   * remainder the least significant bit of input
   */
  class RightShiftResult {

    final SInt result;
    final List<SInt> remainder;

    public RightShiftResult(SInt result,
        List<SInt> remainder) {
      this.result = result;
      this.remainder = remainder;
    }

    public SInt getResult() {
      return result;
    }

    public List<SInt> getRemainder() {
      return remainder;
    }
  }

  class RandomAdditiveMask {

    public final List<Computation<SInt>> bits;
    public final SInt r;

    public RandomAdditiveMask(List<Computation<SInt>> bits, SInt r) {
      this.bits = bits;
      this.r = r;
    }
  }
}
