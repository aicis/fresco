package dk.alexandra.fresco.suite.marlin.resource.storage;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUIntFactory;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestMarlinDummyDataSupplier {

  private void testGetNextRandomElementShare(int noOfParties) {
    List<MarlinDataSupplier<GenericCompUInt>> suppliers = setupSuppliers(noOfParties);
    GenericCompUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinSInt<GenericCompUInt>> shares = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<GenericCompUInt> supplier : suppliers) {
      shares.add(supplier.getNextRandomElementShare());
    }
    MarlinSInt<GenericCompUInt> recombined = recombine(shares);
    assertFalse("Random value was 0 ",
        recombined.getShare().toBigInteger().equals(BigInteger.ZERO));
    assertMacCorrect(recombined, macKey);
  }

  private void testGetNextBitShare(int noOfParties) {
    List<MarlinDataSupplier<GenericCompUInt>> suppliers = setupSuppliers(noOfParties);
    GenericCompUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinSInt<GenericCompUInt>> shares = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<GenericCompUInt> supplier : suppliers) {
      shares.add(supplier.getNextBitShare());
    }
    MarlinSInt<GenericCompUInt> recombined = recombine(shares);
    BigInteger asBitInt = recombined.getShare().toBigInteger();
    assertTrue("Not a bit " + asBitInt,
        asBitInt.equals(BigInteger.ZERO) || asBitInt.equals(BigInteger.ONE));
    assertMacCorrect(recombined, macKey);
  }

  private void testGetInputMask(int noOfParties, int towardParty) {
    List<MarlinDataSupplier<GenericCompUInt>> suppliers = setupSuppliers(noOfParties);
    GenericCompUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinInputMask<GenericCompUInt>> masks = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<GenericCompUInt> supplier : suppliers) {
      masks.add(supplier.getNextInputMask(towardParty));
    }
    GenericCompUInt realValue = null;
    List<MarlinSInt<GenericCompUInt>> shares = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      MarlinInputMask<GenericCompUInt> inputMask = masks.get(i - 1);
      if (i != towardParty) {
        assertTrue(null == inputMask.getOpenValue());
      } else {
        realValue = inputMask.getOpenValue();
      }
      shares.add(inputMask.getMaskShare());
    }
    MarlinSInt<GenericCompUInt> recombined = recombine(shares);
    assertMacCorrect(recombined, macKey);
    assertEquals(realValue.toBigInteger(), recombined.getShare().toBigInteger());
  }

  private void testGetNextTripleShares(int noOfParties) {
    List<MarlinDataSupplier<GenericCompUInt>> suppliers = setupSuppliers(noOfParties);
    GenericCompUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinTriple<GenericCompUInt>> triples = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<GenericCompUInt> supplier : suppliers) {
      triples.add(supplier.getNextTripleShares());
    }
    MarlinTriple<GenericCompUInt> recombined = recombineTriples(triples);
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

  private MarlinSInt<GenericCompUInt> recombine(
      List<MarlinSInt<GenericCompUInt>> shares) {
    return shares.stream().reduce(MarlinSInt::add).get();
  }

  private void assertMacCorrect(MarlinSInt<GenericCompUInt> recombined,
      GenericCompUInt macKey) {
    assertArrayEquals(
        macKey.multiply(recombined.getShare()).toByteArray(),
        recombined.getMacShare().toByteArray()
    );
  }

  private List<MarlinDataSupplier<GenericCompUInt>> setupSuppliers(
      int noOfParties) {
    List<MarlinDataSupplier<GenericCompUInt>> suppliers = new ArrayList<>(
        noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      CompUIntFactory<GenericCompUInt> factory = new GenericCompUIntFactory(64, 64);
      GenericCompUInt macKeyShare = factory.createRandom();
      suppliers.add(new MarlinDummyDataSupplier<>(i + 1, noOfParties, macKeyShare,
          factory));
    }
    return suppliers;
  }

  private GenericCompUInt getMacKeyFromSuppliers(
      List<MarlinDataSupplier<GenericCompUInt>> suppliers) {
    return suppliers.stream()
        .map(MarlinDataSupplier::getSecretSharedKey)
        .reduce(GenericCompUInt::add).get();
  }

  private MarlinTriple<GenericCompUInt> recombineTriples(
      List<MarlinTriple<GenericCompUInt>> triples) {
    List<MarlinSInt<GenericCompUInt>> left = new ArrayList<>(triples.size());
    List<MarlinSInt<GenericCompUInt>> right = new ArrayList<>(triples.size());
    List<MarlinSInt<GenericCompUInt>> product = new ArrayList<>(triples.size());
    for (MarlinTriple<GenericCompUInt> triple : triples) {
      left.add(triple.getLeft());
      right.add(triple.getRight());
      product.add(triple.getProduct());
    }
    return new MarlinTriple<>(recombine(left), recombine(right), recombine(product));
  }

  private void assertTripleValid(MarlinTriple<GenericCompUInt> recombined, GenericCompUInt macKey) {
    assertMacCorrect(recombined.getLeft(), macKey);
    assertMacCorrect(recombined.getRight(), macKey);
    assertMacCorrect(recombined.getRight(), macKey);
    // check that a * b = c
    assertEquals(recombined.getProduct().getShare().toBigInteger(),
        recombined.getLeft().getShare().multiply(recombined.getRight().getShare()).toBigInteger());
  }

}
