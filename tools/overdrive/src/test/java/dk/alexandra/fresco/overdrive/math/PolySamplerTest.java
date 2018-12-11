package dk.alexandra.fresco.overdrive.math;

import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.Is;
import org.hamcrest.number.OrderingComparison;
import org.junit.Before;
import org.junit.Test;

public class PolySamplerTest {

  private final BigInteger modulo = BigInteger.valueOf(17);
  private final BigInteger root = BigInteger.valueOf(3);
  private PolySampler sampler;

  @Before
  public void setUp() {
    byte[] seed = new byte[32];
    new Random(42).nextBytes(seed);
    sampler = new PolySampler(new AesCtrDrbg(seed));
  }

  @Test
  public void uniformCoefficientPoly() {
    CoefficientRingPoly ringPoly = sampler.uniformCoefficientPoly(8, modulo);
    assertThat(ringPoly.getModulus(), Is.is(modulo));
    List<BigInteger> coefficients = ringPoly.getCoefficients();
    assertThat(coefficients, IsCollectionWithSize.hasSize(8));
    for (BigInteger coefficient : coefficients) {
      assertThat(coefficient, OrderingComparison.lessThan(modulo));
      assertThat(coefficient, OrderingComparison.greaterThanOrEqualTo(BigInteger.ZERO));
    }
  }

  @Test
  public void uniformEvaluationPoly() {
    EvaluationRingPoly ringPoly = sampler.uniformEvaluationPoly(8, root, modulo);
    assertThat(ringPoly.getModulus(), Is.is(modulo));
    List<BigInteger> coefficients = ringPoly.getCoefficients();
    assertThat(coefficients, IsCollectionWithSize.hasSize(8));
    for (BigInteger coefficient : coefficients) {
      assertThat(coefficient, OrderingComparison.lessThan(modulo));
      assertThat(coefficient, OrderingComparison.greaterThanOrEqualTo(BigInteger.ZERO));
    }
  }

  @Test
  public void lowWeightPoly() {
    CoefficientRingPoly ringPoly = sampler.lowWeightPoly(16, modulo);
    assertThat(ringPoly.getModulus(), Is.is(modulo));
    List<BigInteger> coefficients = ringPoly.getCoefficients();
    assertThat(coefficients, IsCollectionWithSize.hasSize(16));
    for (BigInteger coefficient : coefficients) {
      assertThat(coefficient, OrderingComparison.lessThanOrEqualTo(BigInteger.ONE));
      assertThat(coefficient, OrderingComparison.greaterThanOrEqualTo(BigInteger.valueOf(-1)));
    }
  }

  @Test
  public void fixedWeightPoly() {
    CoefficientRingPoly ringPoly = sampler.fixedWeightPoly(16, 7, modulo);
    assertThat(ringPoly.getModulus(), Is.is(modulo));
    List<BigInteger> coefficients = ringPoly.getCoefficients();
    assertThat(coefficients, IsCollectionWithSize.hasSize(16));
    for (BigInteger coefficient : coefficients) {
      assertThat(coefficient, OrderingComparison.lessThanOrEqualTo(BigInteger.ONE));
      assertThat(coefficient, OrderingComparison.greaterThanOrEqualTo(BigInteger.valueOf(-1)));
    }
    assertThat(
        coefficients.stream().filter(BigInteger.ZERO::equals).count(),
        Is.is((long) 16 - 7));
  }

  @Test
  public void fixedWeightPolyLargeWeight() {
    CoefficientRingPoly ringPoly = sampler.fixedWeightPoly(16, 15, modulo);
    assertThat(ringPoly.getModulus(), Is.is(modulo));
    List<BigInteger> coefficients = ringPoly.getCoefficients();
    assertThat(coefficients, IsCollectionWithSize.hasSize(16));
    for (BigInteger coefficient : coefficients) {
      assertThat(coefficient, OrderingComparison.lessThanOrEqualTo(BigInteger.ONE));
      assertThat(coefficient, OrderingComparison.greaterThanOrEqualTo(BigInteger.valueOf(-1)));
    }
    assertThat(
        coefficients.stream().filter(BigInteger.ZERO::equals).count(),
        Is.is((long) 16 - 15));
  }

  @Test(expected = IllegalArgumentException.class)
  public void fixedWeightIllegalWeight() {
    sampler.fixedWeightPoly(16, 17, modulo);
  }

  @Test
  public void gaussianPoly() {
    CoefficientRingPoly ringPoly = sampler.gaussianPoly(16, 3.2, modulo);
    assertThat(ringPoly.getModulus(), Is.is(modulo));
    List<BigInteger> coefficients = ringPoly.getCoefficients();
    assertThat(coefficients, IsCollectionWithSize.hasSize(16));
  }
}