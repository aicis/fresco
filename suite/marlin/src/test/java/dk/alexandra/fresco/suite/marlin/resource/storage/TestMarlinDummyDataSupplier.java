package dk.alexandra.fresco.suite.marlin.resource.storage;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUIntFactory;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestMarlinDummyDataSupplier {

  private void testGetNextRandomElementShare(int noOfParties) {
    List<MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt>> suppliers = setupSuppliers(noOfParties);
    GenericUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinSInt<GenericUInt, GenericUInt, GenericUInt>> shares = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt> supplier : suppliers) {
      shares.add(supplier.getNextRandomElementShare());
    }
    MarlinSInt<GenericUInt, GenericUInt, GenericUInt> recombined = recombine(shares);
    assertFalse("Random value was 0 ",
        recombined.getShare().toBigInteger().equals(BigInteger.ZERO));
    assertMacCorrect(recombined, macKey);
  }

  private void testGetNextBitShare(int noOfParties) {
    List<MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt>> suppliers = setupSuppliers(noOfParties);
    GenericUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinSInt<GenericUInt, GenericUInt, GenericUInt>> shares = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt> supplier : suppliers) {
      shares.add(supplier.getNextBitShare());
    }
    MarlinSInt<GenericUInt, GenericUInt, GenericUInt> recombined = recombine(shares);
    BigInteger asBitInt = recombined.getShare().toBigInteger();
    assertTrue("Not a bit " + asBitInt,
        asBitInt.equals(BigInteger.ZERO) || asBitInt.equals(BigInteger.ONE));
    assertMacCorrect(recombined, macKey);
  }

  private void testGetInputMask(int noOfParties, int towardParty) {
    List<MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt>> suppliers = setupSuppliers(noOfParties);
    GenericUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinInputMask<GenericUInt, GenericUInt, GenericUInt>> masks = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt> supplier : suppliers) {
      masks.add(supplier.getNextInputMask(towardParty));
    }
    GenericUInt realValue = null;
    List<MarlinSInt<GenericUInt, GenericUInt, GenericUInt>> shares = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      MarlinInputMask<GenericUInt, GenericUInt, GenericUInt> inputMask = masks.get(i - 1);
      if (i != towardParty) {
        assertTrue(null == inputMask.getOpenValue());
      } else {
        realValue = inputMask.getOpenValue();
      }
      shares.add(inputMask.getMaskShare());
    }
    MarlinSInt<GenericUInt, GenericUInt, GenericUInt> recombined = recombine(shares);
    assertMacCorrect(recombined, macKey);
    assertEquals(realValue.toBigInteger(), recombined.getShare().toBigInteger());
  }

  private void testGetNextTripleShares(int noOfParties) {
    List<MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt>> suppliers = setupSuppliers(noOfParties);
    GenericUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinTriple<GenericUInt, GenericUInt, GenericUInt>> triples = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt> supplier : suppliers) {
      triples.add(supplier.getNextTripleShares());
    }
    MarlinTriple<GenericUInt, GenericUInt, GenericUInt> recombined = recombineTriples(triples);
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

  private MarlinSInt<GenericUInt, GenericUInt, GenericUInt> recombine(
      List<MarlinSInt<GenericUInt, GenericUInt, GenericUInt>> shares) {
    return shares.stream().reduce(MarlinSInt::add).get();
  }

  private void assertMacCorrect(MarlinSInt<GenericUInt, GenericUInt, GenericUInt> recombined,
      GenericUInt macKey) {
    assertArrayEquals(
        macKey.multiply(recombined.getShare()).toByteArray(),
        recombined.getMacShare().toByteArray()
    );
  }

  private List<MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt>> setupSuppliers(
      int noOfParties) {
    List<MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt>> suppliers = new ArrayList<>(
        noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      CompUIntFactory<GenericUInt, GenericUInt, GenericUInt> factory = new GenericCompUIntFactory();
      GenericUInt macKeyShare = factory.createRandom();
      suppliers.add(new MarlinDummyDataSupplier<>(i + 1, noOfParties, macKeyShare,
          factory));
    }
    return suppliers;
  }

  private GenericUInt getMacKeyFromSuppliers(
      List<MarlinDataSupplier<GenericUInt, GenericUInt, GenericUInt>> suppliers) {
    return suppliers.stream()
        .map(MarlinDataSupplier::getSecretSharedKey)
        .reduce(GenericUInt::add).get();
  }

  private MarlinTriple<GenericUInt, GenericUInt, GenericUInt> recombineTriples(
      List<MarlinTriple<GenericUInt, GenericUInt, GenericUInt>> triples) {
    List<MarlinSInt<GenericUInt, GenericUInt, GenericUInt>> left = new ArrayList<>(triples.size());
    List<MarlinSInt<GenericUInt, GenericUInt, GenericUInt>> right = new ArrayList<>(triples.size());
    List<MarlinSInt<GenericUInt, GenericUInt, GenericUInt>> product = new ArrayList<>(triples.size());
    for (MarlinTriple<GenericUInt, GenericUInt, GenericUInt> triple : triples) {
      left.add(triple.getLeft());
      right.add(triple.getRight());
      product.add(triple.getProduct());
    }
    return new MarlinTriple<>(recombine(left), recombine(right), recombine(product));
  }

  private void assertTripleValid(MarlinTriple<GenericUInt, GenericUInt, GenericUInt> recombined, GenericUInt macKey) {
    assertMacCorrect(recombined.getLeft(), macKey);
    assertMacCorrect(recombined.getRight(), macKey);
    assertMacCorrect(recombined.getRight(), macKey);
    // check that a * b = c
    assertEquals(recombined.getProduct().getShare().toBigInteger(),
        recombined.getLeft().getShare().multiply(recombined.getRight().getShare()).toBigInteger());
  }

}
