package dk.alexandra.fresco.overdrive.math;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class EvaluationRingPolyTest {

  private int parameterM;
  private BigInteger modulus;
  private BigInteger modLong;
  private CoefficientRingPoly polyA;
  private CoefficientRingPoly polyLong;
  private BigInteger root;

  @Before
  public void setUp() throws Exception {
    this.parameterM = 16;
    // The modulus must be a prime, p, so that m|p - 1, here we use 2*m + 1 = 17
    this.modulus = BigInteger.valueOf(17);
    this.root = BigInteger.valueOf(3);
    // 2 + 4x + 16x^2 + 15x^3 + 12x^4 + 4x^5 + 5x^6 + 14x^7
    List<BigInteger> coeffsA = Arrays.asList(2, 4, 16, 15, 12, 4, 5, 14).stream()
        .map(BigInteger::valueOf).collect(Collectors.toList());
    this.polyA = new CoefficientRingPoly(coeffsA, this.modulus);
    List<BigInteger> coeffsB = new ArrayList<>(1 << 15);
    Random rand = new Random();
    for (int i = 0; i < (1 << 15); i++) {
      coeffsB.add(new BigInteger(16, rand));
    }
    this.polyLong = new CoefficientRingPoly(coeffsB, BigInteger.valueOf((1 << 16) + 1));
    this.modLong = BigInteger.valueOf((1<<16)+1);
  }

  @Test
  public void testHashCode() {
    fail("Not yet implemented");
  }

  @Test
  public void testFromEvaluations() {
    fail("Not yet implemented");
  }

  @Test
  public void testFromCoefficients() {
    EvaluationRingPoly e = EvaluationRingPoly.fromCoefficients(polyA.getCoefficients(), root, modulus);
    assertEquals(polyA.getCoefficients(), e.getCoefficients());
    EvaluationRingPoly eLong = null;
    for (int i = 0; i < 100; i++) {
      eLong = EvaluationRingPoly.fromCoefficients(polyLong.getCoefficients(), root, modLong);
    }
    System.out.println(polyLong.getCoefficients().size());
    assertEquals(polyLong.getCoefficients(), eLong.getCoefficients());
  }

  @Test
  public void testGetCoefficients() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetM() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetModulus() {
    fail("Not yet implemented");
  }

  @Test
  public void testAdd() {
    fail("Not yet implemented");
  }

  @Test
  public void testPlus() {
    fail("Not yet implemented");
  }

  @Test
  public void testSubtract() {
    fail("Not yet implemented");
  }

  @Test
  public void testMinus() {
    fail("Not yet implemented");
  }

  @Test
  public void testMultiply() {
    fail("Not yet implemented");
  }

  @Test
  public void testTimes() {
    fail("Not yet implemented");
  }

  @Test
  public void testCheckCompatibility() {
    fail("Not yet implemented");
  }

  @Test
  public void testEqualsObject() {
    fail("Not yet implemented");
  }

  @Test
  public void testToString() {
    fail("Not yet implemented");
  }

}
