package dk.alexandra.fresco.framework.util;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;

public class TestArithmeticDummyDataSupplier {

  private final List<BigInteger> moduli = Arrays.asList(
      new BigInteger("251"),
      new BigInteger("340282366920938463463374607431768211283"),
      new BigInteger(
          "2582249878086908589655919172003011874329705792829223512830659356540647622016841194629645353280137831435903171972747493557")
  );

  private void testGetRandomElementShare(int noOfParties, BigInteger modulus) {
    List<ArithmeticDummyDataSupplier> suppliers = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      suppliers.add(new ArithmeticDummyDataSupplier(i + 1, noOfParties, modulus));
    }
    List<Pair<BigInteger, BigInteger>> actual = new ArrayList<>();
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

  private void testGetRandomElementShare(int noOfParties) {
    for (BigInteger modulus : moduli) {
      testGetRandomElementShare(noOfParties, modulus);
    }
  }

  private void testGetRandomBitShare(int noOfParties, BigInteger modulus) {
    List<ArithmeticDummyDataSupplier> suppliers = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      suppliers.add(new ArithmeticDummyDataSupplier(i + 1, noOfParties, modulus));
    }
    List<Pair<BigInteger, BigInteger>> actual = new ArrayList<>();
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

  private void testGetRandomBitShare(int noOfParties) {
    for (BigInteger modulus : moduli) {
      testGetRandomBitShare(noOfParties, modulus);
    }
  }

  private void testGetMultiplicationTripleShares(int noOfParties, BigInteger modulus) {
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

  private void testGetMultiplicationTripleShares(int noOfParties) {
    for (BigInteger modulus : moduli) {
      testGetMultiplicationTripleShares(noOfParties, modulus);
    }
  }

  private void testGetExpPipe(int noOfParties, BigInteger modulus) {
    List<ArithmeticDummyDataSupplier> suppliers = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      suppliers.add(new ArithmeticDummyDataSupplier(i + 1, noOfParties, modulus));
    }
    List<List<Pair<BigInteger, BigInteger>>> actual = new ArrayList<>();
    for (ArithmeticDummyDataSupplier supplier : suppliers) {
      actual.add(supplier.getExpPipe(200));
    }
    // not quite flat map sadly
    List<List<BigInteger>> openPipes = actual.stream()
        .map(p -> p.stream().map(Pair::getFirst).collect(Collectors.toList()))
        .collect(Collectors.toList());
    assertAllEqual(openPipes);
    assertPipeIsCorrect(openPipes.get(0), modulus);
  }

  private void testGetExpPipe(int noOfParties) {
    for (BigInteger modulus : moduli) {
      testGetExpPipe(noOfParties, modulus);
    }
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

  @Test
  public void testGetExpPipes() {
    testGetExpPipe(2);
    testGetExpPipe(3);
    testGetExpPipe(5);
  }

  @Test
  public void testBitsNotAllSame() {
    int noOfParties = 2;
    BigInteger modulus = moduli.get(1);
    ArithmeticDummyDataSupplier supplier = new ArithmeticDummyDataSupplier(1, noOfParties,
        modulus);
    List<BigInteger> bits = new ArrayList<>(1000);
    for (int i = 0; i < 1000; i++) {
      bits.add(supplier.getRandomBitShare().getFirst());
    }
    boolean seenZero = false;
    boolean seenOne = false;
    for (BigInteger bit : bits) {
      seenZero = seenZero || bit.equals(BigInteger.ZERO);
      seenOne = seenOne || bit.equals(BigInteger.ONE);
      if (seenOne && seenZero) {
        break;
      }
    }
    assertTrue(seenOne);
    assertTrue(seenZero);
  }

  @Test
  public void testDummyRecombine() {
    BigInteger modulus = moduli.get(0);
    BigInteger input = new BigInteger(modulus.bitLength(), new Random()).mod(modulus);
    SecretSharer<BigInteger> sharer = new ArithmeticDummyDataSupplier(1, 2,
        modulus).new DummyBigIntegerSharer(modulus,
        new Random());
    assertEquals(input, sharer.recombine(sharer.share(input, 2)));
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

  private <T> void assertAllEqual(List<T> elements) {
    T first = elements.get(0);
    for (T element : elements) {
      assertEquals(first, element);
    }
  }

  private void assertAllBits(List<BigInteger> elements) {
    for (BigInteger element : elements) {
      assertTrue("Element not a bit " + element,
          element.equals(BigInteger.ZERO) || element.equals(BigInteger.ONE));
    }
  }

  private void assertPipeIsCorrect(List<BigInteger> pipe, BigInteger modulus) {
    BigInteger inverted = pipe.get(0);
    BigInteger first = pipe.get(1);
    assertEquals(inverted, first.modInverse(modulus));
    for (int i = 1; i < pipe.size(); i++) {
      assertEquals(first.modPow(BigInteger.valueOf(i), modulus), pipe.get(i));
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
      Pair<BigInteger, BigInteger> left = triple.getLeft();
      Pair<BigInteger, BigInteger> right = triple.getRight();
      Pair<BigInteger, BigInteger> product = triple.getProduct();
      leftValues.add(left.getFirst());
      leftShares.add(left.getSecond());
      rightValues.add(right.getFirst());
      rightShares.add(right.getSecond());
      productValues.add(product.getFirst());
      productShares.add(product.getSecond());
    }
    // sizes are the same
    assertEquals(rightValues.size(), leftValues.size());
    assertEquals(leftValues.size(), productValues.size());
    // left * right = product
    for (int i = 0; i < leftValues.size(); i++) {
      assertEquals(productValues.get(i),
          leftValues.get(i).multiply(rightValues.get(i)).mod(modulus));
    }
    // all left values the same; left = recombine([left]); all left shares different
    leftValues.add(MathUtils.sum(leftShares, modulus));
    assertAllEqual(leftValues);
    assertAllDifferent(leftShares);
    rightValues.add(MathUtils.sum(rightShares, modulus));
    assertAllEqual(rightValues);
    assertAllDifferent(rightShares);
    productValues.add(MathUtils.sum(productShares, modulus));
    assertAllEqual(productValues);
    assertAllDifferent(productShares);
  }

}
