package dk.alexandra.fresco.suite.marlin.resource.storage;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import dk.alexandra.fresco.suite.marlin.datatypes.MutableUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.MutableUInt128Factory;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestMarlinDummyDataSupplier {

  private void testGetNextRandomElementShare(int noOfParties) {
    List<MarlinDataSupplier<MutableUInt128>> suppliers = setupSuppliers(noOfParties);
    MutableUInt128 macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinElement<MutableUInt128>> shares = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<MutableUInt128> supplier : suppliers) {
      shares.add(supplier.getNextRandomElementShare().getValue());
    }
    MarlinElement<MutableUInt128> recombined = recombine(shares);
    assertFalse("Random value was 0 ",
        recombined.getShare().toBigInteger().equals(BigInteger.ZERO));
    assertMacCorrect(recombined, macKey);
  }

  private void testGetNextBitShare(int noOfParties) {
    List<MarlinDataSupplier<MutableUInt128>> suppliers = setupSuppliers(noOfParties);
    MutableUInt128 macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinElement<MutableUInt128>> shares = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<MutableUInt128> supplier : suppliers) {
      shares.add(supplier.getNextBitShare().getValue());
    }
    MarlinElement<MutableUInt128> recombined = recombine(shares);
    BigInteger asBitInt = recombined.getShare().toBigInteger();
    assertTrue("Not a bit " + asBitInt,
        asBitInt.equals(BigInteger.ZERO) || asBitInt.equals(BigInteger.ONE));
    assertMacCorrect(recombined, macKey);
  }

  private void testGetInputMask(int noOfParties, int towardParty) {
    List<MarlinDataSupplier<MutableUInt128>> suppliers = setupSuppliers(noOfParties);
    MutableUInt128 macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinInputMask<MutableUInt128>> masks = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<MutableUInt128> supplier : suppliers) {
      masks.add(supplier.getNextInputMask(towardParty));
    }
    MutableUInt128 realValue = null;
    List<MarlinElement<MutableUInt128>> shares = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      MarlinInputMask<MutableUInt128> inputMask = masks.get(i - 1);
      if (i != towardParty) {
        assertTrue(null == inputMask.getOpenValue());
      } else {
        realValue = inputMask.getOpenValue();
      }
      shares.add(inputMask.getMaskShare());
    }
    MarlinElement<MutableUInt128> recombined = recombine(shares);
    assertMacCorrect(recombined, macKey);
    assertEquals(realValue.toBigInteger(), recombined.getShare().toBigInteger());
  }

  private void testGetNextTripleShares(int noOfParties) {
    List<MarlinDataSupplier<MutableUInt128>> suppliers = setupSuppliers(noOfParties);
    MutableUInt128 macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinTriple<MutableUInt128>> triples = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<MutableUInt128> supplier : suppliers) {
      triples.add(supplier.getNextTripleShares());
    }
    MarlinTriple<MutableUInt128> recombined = recombineTriples(triples);
    assertTripleValid(recombined, macKey);
  }

  @Test
  public void testGetNextRandomElementShare() {
    testGetNextRandomElementShare(2);
    testGetNextRandomElementShare(3);
    testGetNextRandomElementShare(5);
  }

  @Test
  public void testGetNextBitShare() {
    testGetNextBitShare(2);
    testGetNextBitShare(3);
    testGetNextBitShare(5);
  }

  @Test
  public void testGetInputMask() {
    List<Integer> partyNumbers = Arrays.asList(2, 3, 5);
    for (int noOfParties : partyNumbers) {
      for (int i = 1; i <= noOfParties; i++) {
        testGetInputMask(noOfParties, i);
      }
    }
  }

  @Test
  public void testGetNextTripleShares() {
    testGetNextTripleShares(2);
    testGetNextTripleShares(3);
    testGetNextTripleShares(5);
  }

  private MarlinElement<MutableUInt128> recombine(List<MarlinElement<MutableUInt128>> shares) {
    return shares.stream().reduce(MarlinElement::add).get();
  }

  private void assertMacCorrect(MarlinElement<MutableUInt128> recombined, MutableUInt128 macKey) {
    assertArrayEquals(
        macKey.multiply(recombined.getShare()).toByteArray(),
        recombined.getMacShare().toByteArray()
    );
  }

  private List<MarlinDataSupplier<MutableUInt128>> setupSuppliers(int noOfParties) {
    List<MarlinDataSupplier<MutableUInt128>> suppliers = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      BigUIntFactory<MutableUInt128> factory = new MutableUInt128Factory();
      MutableUInt128 macKeyShare = factory.createRandom();
      suppliers.add(new MarlinDummyDataSupplier<>(i + 1, noOfParties, macKeyShare,
          factory));
    }
    return suppliers;
  }

  private MutableUInt128 getMacKeyFromSuppliers(
      List<MarlinDataSupplier<MutableUInt128>> suppliers) {
    return suppliers.stream()
        .map(MarlinDataSupplier::getSecretSharedKey)
        .reduce(MutableUInt128::add)
        .orElse(new MutableUInt128(0));
  }

  private MarlinTriple<MutableUInt128> recombineTriples(
      List<MarlinTriple<MutableUInt128>> triples) {
    List<MarlinElement<MutableUInt128>> left = new ArrayList<>(triples.size());
    List<MarlinElement<MutableUInt128>> right = new ArrayList<>(triples.size());
    List<MarlinElement<MutableUInt128>> product = new ArrayList<>(triples.size());
    for (MarlinTriple<MutableUInt128> triple : triples) {
      left.add(triple.getLeft());
      right.add(triple.getRight());
      product.add(triple.getProduct());
    }
    return new MarlinTriple<>(recombine(left), recombine(right), recombine(product));
  }

  private void assertTripleValid(MarlinTriple<MutableUInt128> recombined, MutableUInt128 macKey) {
    assertMacCorrect(recombined.getLeft(), macKey);
    assertMacCorrect(recombined.getRight(), macKey);
    assertMacCorrect(recombined.getRight(), macKey);
    // check that a * b = c
    assertEquals(recombined.getProduct().getShare().toBigInteger(),
        recombined.getLeft().getShare().multiply(recombined.getRight().getShare()).toBigInteger());
  }

}
