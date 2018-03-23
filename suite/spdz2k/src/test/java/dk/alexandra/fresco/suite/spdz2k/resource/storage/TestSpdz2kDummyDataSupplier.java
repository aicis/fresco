package dk.alexandra.fresco.suite.spdz2k.resource.storage;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.GenericCompUIntFactory;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TestSpdz2kDummyDataSupplier {

  private void testGetNextRandomElementShare(int noOfParties) {
    List<Spdz2kDataSupplier<GenericCompUInt>> suppliers = setupSuppliers(noOfParties);
    GenericCompUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<Spdz2kSInt<GenericCompUInt>> shares = new ArrayList<>(noOfParties);
    for (Spdz2kDataSupplier<GenericCompUInt> supplier : suppliers) {
      shares.add(supplier.getNextRandomElementShare());
    }
    Spdz2kSInt<GenericCompUInt> recombined = recombine(shares);
    assertFalse("Random value was 0 ",
        recombined.getShare().toBigInteger().equals(BigInteger.ZERO));
    assertMacCorrect(recombined, macKey);
  }

  private void testGetNextBitShare(int noOfParties) {
    List<Spdz2kDataSupplier<GenericCompUInt>> suppliers = setupSuppliers(noOfParties);
    GenericCompUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<Spdz2kSInt<GenericCompUInt>> shares = new ArrayList<>(noOfParties);
    for (Spdz2kDataSupplier<GenericCompUInt> supplier : suppliers) {
      shares.add(supplier.getNextBitShare());
    }
    Spdz2kSInt<GenericCompUInt> recombined = recombine(shares);
    BigInteger asBitInt = recombined.getShare().toBigInteger();
    assertTrue("Not a bit " + asBitInt,
        asBitInt.equals(BigInteger.ZERO) || asBitInt.equals(BigInteger.ONE));
    assertMacCorrect(recombined, macKey);
  }

  private void testGetInputMask(int noOfParties, int towardParty) {
    List<Spdz2kDataSupplier<GenericCompUInt>> suppliers = setupSuppliers(noOfParties);
    GenericCompUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<Spdz2kInputMask<GenericCompUInt>> masks = new ArrayList<>(noOfParties);
    for (Spdz2kDataSupplier<GenericCompUInt> supplier : suppliers) {
      masks.add(supplier.getNextInputMask(towardParty));
    }
    GenericCompUInt realValue = null;
    List<Spdz2kSInt<GenericCompUInt>> shares = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      Spdz2kInputMask<GenericCompUInt> inputMask = masks.get(i - 1);
      if (i != towardParty) {
        assertTrue(null == inputMask.getOpenValue());
      } else {
        realValue = inputMask.getOpenValue();
      }
      shares.add(inputMask.getMaskShare());
    }
    Spdz2kSInt<GenericCompUInt> recombined = recombine(shares);
    assertMacCorrect(recombined, macKey);
    assertEquals(realValue.toBigInteger(), recombined.getShare().toBigInteger());
  }

  private void testGetNextTripleShares(int noOfParties) {
    List<Spdz2kDataSupplier<GenericCompUInt>> suppliers = setupSuppliers(noOfParties);
    GenericCompUInt macKey = getMacKeyFromSuppliers(suppliers);
    List<Spdz2kTriple<GenericCompUInt>> triples = new ArrayList<>(noOfParties);
    for (Spdz2kDataSupplier<GenericCompUInt> supplier : suppliers) {
      triples.add(supplier.getNextTripleShares());
    }
    Spdz2kTriple<GenericCompUInt> recombined = recombineTriples(triples);
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

  private Spdz2kSInt<GenericCompUInt> recombine(
      List<Spdz2kSInt<GenericCompUInt>> shares) {
    return shares.stream().reduce(Spdz2kSInt::add).get();
  }

  private void assertMacCorrect(Spdz2kSInt<GenericCompUInt> recombined,
      GenericCompUInt macKey) {
    assertArrayEquals(
        macKey.multiply(recombined.getShare()).toByteArray(),
        recombined.getMacShare().toByteArray()
    );
  }

  private List<Spdz2kDataSupplier<GenericCompUInt>> setupSuppliers(
      int noOfParties) {
    List<Spdz2kDataSupplier<GenericCompUInt>> suppliers = new ArrayList<>(
        noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      CompUIntFactory<GenericCompUInt> factory = new GenericCompUIntFactory(64, 64);
      GenericCompUInt macKeyShare = factory.createRandom();
      suppliers.add(new Spdz2kDummyDataSupplier<>(i + 1, noOfParties, macKeyShare,
          factory));
    }
    return suppliers;
  }

  private GenericCompUInt getMacKeyFromSuppliers(
      List<Spdz2kDataSupplier<GenericCompUInt>> suppliers) {
    return suppliers.stream()
        .map(Spdz2kDataSupplier::getSecretSharedKey)
        .reduce(GenericCompUInt::add).get();
  }

  private Spdz2kTriple<GenericCompUInt> recombineTriples(
      List<Spdz2kTriple<GenericCompUInt>> triples) {
    List<Spdz2kSInt<GenericCompUInt>> left = new ArrayList<>(triples.size());
    List<Spdz2kSInt<GenericCompUInt>> right = new ArrayList<>(triples.size());
    List<Spdz2kSInt<GenericCompUInt>> product = new ArrayList<>(triples.size());
    for (Spdz2kTriple<GenericCompUInt> triple : triples) {
      left.add(triple.getLeft());
      right.add(triple.getRight());
      product.add(triple.getProduct());
    }
    return new Spdz2kTriple<>(recombine(left), recombine(right), recombine(product));
  }

  private void assertTripleValid(Spdz2kTriple<GenericCompUInt> recombined, GenericCompUInt macKey) {
    assertMacCorrect(recombined.getLeft(), macKey);
    assertMacCorrect(recombined.getRight(), macKey);
    assertMacCorrect(recombined.getRight(), macKey);
    // check that a * b = c
    assertEquals(recombined.getProduct().getShare().toBigInteger(),
        recombined.getLeft().getShare().multiply(recombined.getRight().getShare()).toBigInteger());
  }

}
