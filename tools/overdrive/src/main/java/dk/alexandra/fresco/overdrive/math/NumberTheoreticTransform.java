package dk.alexandra.fresco.overdrive.math;

import java.math.BigInteger;
import java.util.List;

/**
 * The number theoretic transform is the Fourier transform in <i>Z_q</i> for some prime <i>q</i>.
 */
interface NumberTheoreticTransform {

  /**
   * Computes the forward transform of a list of coefficients.
   *
   * @param coefficients the coefficients
   * @return the forward transform
   */
  List<BigInteger> nnt(List<BigInteger> coefficients);

  /**
   * Computes the inverse transform of a list of evaluation points as computed by
   * #{@link NumberTheoreticTransform#nnt(List)}.
   *
   * @param evaluations the list of evaluation points
   * @return the inverse transform
   */
  List<BigInteger> nntInverse(List<BigInteger> evaluations);

}
