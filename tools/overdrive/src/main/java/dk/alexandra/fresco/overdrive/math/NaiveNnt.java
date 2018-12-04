package dk.alexandra.fresco.overdrive.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A naive implementation of the number theoretic transform. Useful for comparison with the FFT
 * versions.
 */
final class NaiveNnt implements NumberTheoreticTransform {

  private BigInteger root;
  private BigInteger modulus;
  private BigInteger rootInverse;

  NaiveNnt(BigInteger root, BigInteger modulus) {
    this.root = root;
    this.modulus = modulus;
    this.rootInverse = root.modInverse(modulus);
  }

  @Override
  public List<BigInteger> nnt(List<BigInteger> coefficients) {
    BigInteger myRoot = root;
    List<BigInteger> evaluations = nntInternal(coefficients, myRoot);
    return evaluations;
  }

  @Override
  public List<BigInteger> nntInverse(List<BigInteger> evaluations) {
    List<BigInteger> coefficients = nntInternal(evaluations, rootInverse);
    BigInteger sizeInv = BigInteger.valueOf(evaluations.size()).modInverse(modulus);
    for (int i = 0; i < coefficients.size(); i++) {
      coefficients.set(i, coefficients.get(i).multiply(sizeInv).mod(modulus));
    }
    return coefficients;
  }

  private List<BigInteger> nntInternal(List<BigInteger> coefficients, BigInteger myRoot) {
    CoefficientRingPoly poly = new CoefficientRingPoly(coefficients, modulus);
    List<BigInteger> evaluations = new ArrayList<>(coefficients.size());
    BigInteger evaluationPoint = BigInteger.ONE;
    for (int i = 0; i < coefficients.size(); i++) {
      evaluations.add(poly.eval(evaluationPoint));
      evaluationPoint = evaluationPoint.multiply(myRoot).mod(modulus);
    }
    return evaluations;
  }

}
