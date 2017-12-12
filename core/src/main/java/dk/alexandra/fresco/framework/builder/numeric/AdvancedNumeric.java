package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;

/**
 * Interface for advanced functionality applicable to numeric type applications. 
 */
public interface AdvancedNumeric extends ComputationDirectory {

  /**
   * Calculates the sum of all elements in the list.
   *
   * @param elements the elements to sum
   * @return A deferred result computing the sum of the elements
   */
  DRes<SInt> sum(List<DRes<SInt>> elements);

  /**
   * Calculates the product of all elements in the list.
   *
   * @param elements the elements to sum
   * @return A deferred result computing the product of the elements
   */
  DRes<SInt> product(List<DRes<SInt>> elements);

  /**
   * This protocol calculates an approximation of <code>floor(dividend / divisor)</code>, which will
   * be either correct or slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return A deferred result computing an approximation of <i>dividend / divisor</i>.
   */
  DRes<SInt> div(DRes<SInt> dividend, BigInteger divisor);
  
  /**
   * This protocol calculates an approximation of <code>floor(dividend / divisor)</code>, which will
   * be either correct or slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return A deferred result computing an approximation of <i>dividend / divisor</i>.
   */
  DRes<SInt> div(DRes<SInt> dividend, DRes<SInt> divisor);

  /**
   * This protocol calculates an approximation of <code>floor(dividend / divisor)</code>, which will
   * be either correct or slightly smaller than the correct result.
   *
   * @param dividend The dividend.
   * @param divisor The divisor.
   * @return A deferred result computing quotient and remainder.
   */
  DRes<SInt> mod(DRes<SInt> dividend, BigInteger divisor);

  /**
   * Convert an integer to an list of bits, with index 0 being the least significant bit.
   *
   * @param in SInt
   * @return A deferred result computing the list of bits
   */
  DRes<List<SInt>> toBits(DRes<SInt> in, int maxInputLength);

  /**
   * Computes the exponentiation of x^e
   * @param x The base
   * @param e The exponent
   * @param maxExponentLength The maximum length of the exponent.
   * @return A deferred result computing x^e
   */
  DRes<SInt> exp(DRes<SInt> x, DRes<SInt> e, int maxExponentLength);

  /**
   * Computes the exponentiation of x^e
   * @param x The base
   * @param e The exponent
   * @param maxExponentLength The maximum length of the exponent.
   * @return A deferred result computing x^e
   */
  DRes<SInt> exp(BigInteger x, DRes<SInt> e, int maxExponentLength);

  /**
   * Computes the exponentiation of x^e.
   * @param x The base
   * @param e The exponent
   * @return A deferred result computing x^e
   */
  DRes<SInt> exp(DRes<SInt> x, BigInteger e);

  /**
   * @param input The input.
   * @param maxInputLength An upper bound for <i>log<sub>2</sub>(input)</i>.
   * @return A deferred result computing an approximation of the square root of the input.
   */
  DRes<SInt> sqrt(DRes<SInt> input, int maxInputLength);

  /**
   * Calculating the natural logarithm of a given input.
   *
   * @param input The input.
   * @param maxInputLength An upper bound for the bit length of the input.
   * @return A deferred result computing the natural logarithm of the input.
   */
  DRes<SInt> log(DRes<SInt> input, int maxInputLength);

  /**
   * Computes the inner product between two vectors.
   * 
   * @param vectorA The first vector
   * @param vectorB The second vector
   * @return A deferred result computing the inner product of the two given vectors
   */
  DRes<SInt> innerProduct(List<DRes<SInt>> vectorA, List<DRes<SInt>> vectorB);

  /**
   * Computes the inner product between a public vector and a secret vector. 
   * 
   * @param vectorA The public vector
   * @param vectorB The secret vector
   * @return A deferred result computing the inner product of the two given vectors
   */
  DRes<SInt> innerProductWithPublicPart(List<BigInteger> vectorA, List<DRes<SInt>> vectorB);

  /**
   * Creates a string of random bits.
   * @param noOfBits The amount of bits to create - i.e. the bit string length.
   * @return A container holding the bit string once evaluated.
   */
  DRes<RandomAdditiveMask> additiveMask(int noOfBits);

  /**
   * @param input input.
   * @return A deferred result computing input >> 1
   */
  DRes<SInt> rightShift(DRes<SInt> input);

  /**
   * @param input input.
   * @param shifts Number of shifts
   * @return A deferred result computing input >> shifts
   */
  DRes<SInt> rightShift(DRes<SInt> input, int shifts);
  
  /**
   * @param input input
   * @return A deferred result computing<br> 
   *         result: input >> 1<br>
   *         remainder: The <code>shifts</code> least significant bits of the input with the least
   *         significant having index 0.
   */
  DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return A deferred result computing <br>
   *         result: input >> shifts<br>
   *         remainder: The <code>shifts</code> least significant bits of the input with the least
   *         significant having index 0.
   */
  DRes<RightShiftResult> rightShiftWithRemainder(DRes<SInt> input, int shifts);

  /**
   * Computes the bit length of the input.
   * 
   * @param input The number to know the bit length of
   * @param maxBitLength The maximum bit length this number can have (if unknown, set this to the
   *        modulus bit size)
   * @return A deferred result computing the bit length of the input number.
   */
  DRes<SInt> bitLength(DRes<SInt> input, int maxBitLength);

  /**
   * Compute the inverse of x within the field of operation
   * @param x The element to take the inverse of
   * @return A deferred result computing x^-1 mod p where p is the modulus of the field.
   */
  DRes<SInt> invert(DRes<SInt> x);

  /**
   * Selects left or right based on condition.
   *
   * @param condition the Computation holding the condition on which to select. Must be either 0 or
   *        1.
   * @param left the Computation holding the left argument.
   * @param right the Computation holding the right argument.
   * @return a computation holding either left or right depending on the condition.
   */
  DRes<SInt> condSelect(DRes<SInt> condition, DRes<SInt> left, DRes<SInt> right);

  /**
   * Swaps <code>left</code> and <code>right</code> if <code>condition</code> is 1, keeps original
   * order otherwise. Returns result as a pair.
   * 
   * @param condition must be 0 or 1.
   * @param left The left argument
   * @param right The right argument
   * @return A deferred result computing a pair containing [left, right] if the condition is 0 and
   *         [right, left] if condition is 1.
   */
  DRes<Pair<DRes<SInt>, DRes<SInt>>> swapIf(DRes<SInt> condition, DRes<SInt> left,
      DRes<SInt> right);

  /**
   * Container holding the deferred result and remainder of shifting a number.  
   */
  class RightShiftResult {

    final SInt result;
    final List<SInt> remainder;

    public RightShiftResult(SInt result, List<SInt> remainder) {
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
