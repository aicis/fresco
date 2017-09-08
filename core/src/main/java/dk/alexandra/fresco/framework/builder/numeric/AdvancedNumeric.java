package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;

public interface AdvancedNumeric extends ComputationDirectory {

  /**
   * Calculates the sum of all elements in the list.
   *
   * @param elements the elements to sum
   * @return the sum of the elements
   */
  DRes<SInt> sum(List<DRes<SInt>> elements);

  /**
   * Calculates the product of all elements in the list.
   *
   * @param elements the elements to sum
   * @return the product of the elements
   */
  DRes<SInt> product(List<DRes<SInt>> elements);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return An approximation of <i>dividend / divisor</i>.
   */
  DRes<SInt> div(DRes<SInt> dividend, BigInteger divisor);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return quotient and remainder.
   */
  DRes<SInt> mod(DRes<SInt> dividend, BigInteger divisor);

  /**
   * This protocol calculates an approximation of
   * <code>floor(dividend / divisor)</code>, which will be either correct or
   * slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return An approximation of <i>dividend / divisor</i>.
   */
  DRes<SInt> div(DRes<SInt> dividend, DRes<SInt> divisor);

  /**
   * Convert an integer to an list of bits, with index 0 being the least
   * significant bit
   *
   * @param in SInt
   * @return the list of bits
   */
  DRes<List<SInt>> toBits(DRes<SInt> in, int maxInputLength);


  DRes<SInt> exp(DRes<SInt> x, DRes<SInt> e, int maxExponentLength);

  DRes<SInt> exp(BigInteger x, DRes<SInt> e, int maxExponentLength);

  DRes<SInt> exp(DRes<SInt> x, BigInteger e);

  /**
   * @param input The input.
   * @param maxInputLength An upper bound for <i>log<sub>2</sub>(input)</i>.
   * @return An approximation of the square root of the input.
   */
  DRes<SInt> sqrt(DRes<SInt> input, int maxInputLength);

  /**
   * Calculating the natural logarithm of a given input.
   *
   * @param input The input.
   * @param maxInputLength An upper bound for the bit length of the input.
   * @return The natural logarithm of the input.
   */
  DRes<SInt> log(DRes<SInt> input, int maxInputLength);

  DRes<SInt> dot(List<DRes<SInt>> aVector, List<DRes<SInt>> bVector);

  DRes<SInt> openDot(List<BigInteger> aVector, List<DRes<SInt>> bVector);

  DRes<RandomAdditiveMask> additiveMask(int noOfBits);

  /**
   * @param input input
   * @return result input >> 1
   */
  DRes<SInt> rightShift(DRes<SInt> input);

  /**
   * @param input input
   * @return result: input >> 1<br> remainder: The <code>shifts</code> least significant bits of the
   * input with the least significant having index 0.
   */
  DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return result input >> shifts
   */
  DRes<SInt> rightShift(DRes<SInt> input, int shifts);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return result: input >> shifts<br> remainder: The <code>shifts</code> least significant bits
   * of the input with the least significant having index 0.
   */
  DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input,
      int shifts);

  DRes<SInt> bitLength(DRes<SInt> input, int maxBitLength);

  DRes<SInt> invert(DRes<SInt> x);

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

    public final List<DRes<SInt>> bits;
    public final SInt r;

    public RandomAdditiveMask(List<DRes<SInt>> bits, SInt r) {
      this.bits = bits;
      this.r = r;
    }
  }
}
