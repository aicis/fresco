package dk.alexandra.fresco.suite.spdz.storage;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.SameTypePair;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class TestArithmeticDummyDataSupplier {

  private final BigInteger modulus = new BigInteger("251");

  private void testGetRandomElementShare(int noOfParties) {
    List<ArithmeticDummyDataSupplier> suppliers = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      suppliers.add(new ArithmeticDummyDataSupplier(i + 1, noOfParties, modulus));
    }
    List<SameTypePair<BigInteger>> actual = new ArrayList<>();
    for (ArithmeticDummyDataSupplier supplier : suppliers) {
      actual.add(supplier.getRandomElementShare());
    }
    List<BigInteger> elements = actual.stream()
        .map(Pair::getFirst)
        .collect(Collectors.toList());
    List<BigInteger> shares = actual.stream()
        .map(Pair::getSecond)
        .collect(Collectors.toList());
    BigInteger recombined = MathUtils.sum(shares, modulus);
    BigInteger first = elements.get(0);
    assertEquals(first, recombined);
    for (BigInteger element : elements) {
      assertEquals(first, element);
    }
    assertAllDifferent(shares);
  }

  private void testGetRandomBitShare(int noOfParties) {
    List<ArithmeticDummyDataSupplier> suppliers = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      suppliers.add(new ArithmeticDummyDataSupplier(i + 1, noOfParties, modulus));
    }
    List<SameTypePair<BigInteger>> actual = new ArrayList<>();
    for (ArithmeticDummyDataSupplier supplier : suppliers) {
      actual.add(supplier.getRandomBitShare());
    }
    List<BigInteger> elements = actual.stream()
        .map(Pair::getFirst)
        .collect(Collectors.toList());
    List<BigInteger> shares = actual.stream()
        .map(Pair::getSecond)
        .collect(Collectors.toList());
    BigInteger recombined = MathUtils.sum(shares, modulus);
    BigInteger first = elements.get(0);
    assertEquals(first, recombined);
    for (BigInteger element : elements) {
      assertEquals(first, element);
    }
    assertAllDifferent(shares);
    assertAllBits(elements);
  }

  private void testGetMultiplicationTripleShares(int noOfParties) {
    List<ArithmeticDummyDataSupplier> suppliers = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      suppliers.add(new ArithmeticDummyDataSupplier(i + 1, noOfParties, modulus));
    }
    List<MultiplicationTripleShares> actual = new ArrayList<>();
    for (ArithmeticDummyDataSupplier supplier : suppliers) {
      actual.add(supplier.getMultiplicationTripleShares());
    }
    assertMultiplicationTriplesValid(actual, modulus);
  }

  @Test
  public void testGetRandomElementShareTwoParties() {
    testGetRandomElementShare(2);
  }

  @Test
  public void testGetRandomElementShareThreeParties() {
    testGetRandomElementShare(3);
  }

  @Test
  public void testGetRandomElementShareFiveParties() {
    testGetRandomElementShare(5);
  }

  @Test
  public void testGetRandomBitShareTwoParties() {
    testGetRandomBitShare(2);
  }

  @Test
  public void testGetRandomBitShareThreeParties() {
    testGetRandomBitShare(3);
  }

  @Test
  public void testGetRandomBitShareFiveParties() {
    testGetRandomBitShare(5);
  }

  @Test
  public void testGetMultiplicationTripleSharesTwo() {
    testGetMultiplicationTripleShares(2);
  }

  @Test
  public void testGetMultiplicationTripleSharesThree() {
    testGetMultiplicationTripleShares(3);
  }

  @Test
  public void testGetMultiplicationTripleSharesFive() {
    testGetMultiplicationTripleShares(5);
  }

  private void assertAllDifferent(List<BigInteger> elements) {
    for (int i = 0; i < elements.size(); i++) {
      for (int j = 0; j < elements.size(); j++) {
        if (i != j) {
          assertNotEquals(elements.get(i), elements.get(j));
        }
      }
    }
  }

  private void assertAllEqual(List<BigInteger> elements) {
    BigInteger first = elements.get(0);
    for (BigInteger element : elements) {
      assertEquals(first, element);
    }
  }

  private void assertAllBits(List<BigInteger> elements) {
    for (BigInteger element : elements) {
      assertTrue("Element not a bit " + element,
          element.equals(BigInteger.ZERO) || element.equals(BigInteger.ONE));
    }
  }

  private void assertMultiplicationTriplesValid(List<MultiplicationTripleShares> triples,
      BigInteger modulus) {
    List<BigInteger> leftValues = new ArrayList<>(triples.size());
    List<BigInteger> leftShares = new ArrayList<>(triples.size());
    List<BigInteger> rightValues = new ArrayList<>(triples.size());
    List<BigInteger> rightShares = new ArrayList<>(triples.size());
    List<BigInteger> productValues = new ArrayList<>(triples.size());
    List<BigInteger> productShares = new ArrayList<>(triples.size());
    for (MultiplicationTripleShares triple : triples) {
      SameTypePair<BigInteger> left = triple.getLeft();
      SameTypePair<BigInteger> right = triple.getRight();
      SameTypePair<BigInteger> product = triple.getProduct();
      leftValues.add(left.getFirst());
      leftShares.add(left.getSecond());
      rightValues.add(right.getFirst());
      rightShares.add(right.getSecond());
      productValues.add(product.getFirst());
      productShares.add(product.getSecond());
    }
    leftValues.add(MathUtils.sum(leftShares, modulus));
    assertAllEqual(leftValues);
    assertAllDifferent(leftShares);
    rightValues.add(MathUtils.sum(rightShares, modulus));
    assertAllEqual(rightValues);
    assertAllDifferent(rightShares);
    productValues.add(MathUtils.sum(productShares, modulus));
    assertAllEqual(productValues);
    assertAllDifferent(productShares);
    assertEquals(rightValues.size(), leftValues.size());
    assertEquals(leftValues.size(), productValues.size());
    for (int i = 0; i < leftValues.size(); i++) {
      assertEquals(productValues.get(i),
          leftValues.get(i).multiply(rightValues.get(i)).mod(modulus));
    }
  }

}
