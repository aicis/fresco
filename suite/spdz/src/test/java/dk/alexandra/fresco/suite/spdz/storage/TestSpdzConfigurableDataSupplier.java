package dk.alexandra.fresco.suite.spdz.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class TestSpdzConfigurableDataSupplier {

  private final List<BigInteger> moduli = Arrays.asList(
      new BigInteger("251"),
      new BigInteger("340282366920938463463374607431768211283"),
      new BigInteger(
          "2582249878086908589655919172003011874329705792829223512830659356540647622016841194629645353280137831435903171972747493557")
  );

  private List<SpdzConfigurableDataSupplier> setupSuppliers(int noOfParties,
      BigInteger modulus) {
    List<SpdzConfigurableDataSupplier> suppliers = new ArrayList<>(noOfParties);
    Random random = new Random();
    for (int i = 0; i < noOfParties; i++) {
      BigInteger macKeyShare = new BigInteger(modulus.bitLength(), random).mod(modulus);
      suppliers.add(new SpdzConfigurableDataSupplier(i + 1, noOfParties, modulus, macKeyShare));
    }
    return suppliers;
  }

  private BigInteger getMacKeyFromSuppliers(List<SpdzConfigurableDataSupplier> suppliers) {
    BigInteger macKey = BigInteger.ZERO;
    for (SpdzConfigurableDataSupplier supplier : suppliers) {
      macKey = macKey.add(supplier.getSecretSharedKey());
    }
    return macKey.mod(suppliers.get(0).getModulus());
  }


  private void testGetNextTriple(int noOfParties, BigInteger modulus) {
    List<SpdzConfigurableDataSupplier> suppliers = setupSuppliers(noOfParties, modulus);
    BigInteger macKey = getMacKeyFromSuppliers(suppliers);
    List<SpdzTriple> triples = new ArrayList<>(noOfParties);
    for (SpdzConfigurableDataSupplier supplier : suppliers) {
      triples.add(supplier.getNextTriple());
    }
    SpdzTriple recombined = recombineTriples(triples);
    assertTripleValid(recombined, macKey, modulus);
  }

  private void testGetNextTriple(int noOfParties) {
    for (BigInteger modulus : moduli) {
      testGetNextTriple(noOfParties, modulus);
    }
  }

  private void testGetNextInputMask(int noOfParties, int towardParty, BigInteger modulus) {
    List<SpdzConfigurableDataSupplier> suppliers = setupSuppliers(noOfParties, modulus);
    BigInteger macKey = getMacKeyFromSuppliers(suppliers);
    List<SpdzInputMask> masks = new ArrayList<>(noOfParties);
    for (SpdzConfigurableDataSupplier supplier : suppliers) {
      masks.add(supplier.getNextInputMask(towardParty));
    }
    BigInteger realValue = null;
    List<SpdzElement> shares = new ArrayList<>(noOfParties);
    for (int i = 0; i < noOfParties; i++) {
      SpdzInputMask spdzInputMask = masks.get(i);
      if (i + 1 != towardParty) {
        assertEquals(null, spdzInputMask.getRealValue());
      } else {
        realValue = spdzInputMask.getRealValue();
      }
      shares.add(spdzInputMask.getMask());
    }
    SpdzElement recombined = recombine(shares);
    assertMacCorrect(recombined, macKey, modulus);
    assertEquals(realValue, recombined.getShare());
  }

  private void testGetNextInputMask(int noOfParties, int towardParty) {
    for (BigInteger modulus : moduli) {
      testGetNextInputMask(noOfParties, towardParty, modulus);
    }
  }

  private void testGetNextBit(int noOfParties, BigInteger modulus) {
    List<SpdzConfigurableDataSupplier> suppliers = setupSuppliers(noOfParties, modulus);
    BigInteger macKey = getMacKeyFromSuppliers(suppliers);
    List<SpdzElement> bitShares = new ArrayList<>(noOfParties);
    for (SpdzConfigurableDataSupplier supplier : suppliers) {
      bitShares.add(supplier.getNextBit().value);
    }
    SpdzElement recombined = recombine(bitShares);
    assertMacCorrect(recombined, macKey, modulus);
    BigInteger value = recombined.getShare();
    assertTrue("Value not a bit " + value,
        value.equals(BigInteger.ZERO) || value.equals(BigInteger.ONE));
  }

  private void testGetNextBit(int noOfParties) {
    for (BigInteger modulus : moduli) {
      testGetNextBit(noOfParties, modulus);
    }
  }

  private void testGetNextRandomFieldElement(int noOfParties, BigInteger modulus) {
    List<SpdzConfigurableDataSupplier> suppliers = setupSuppliers(noOfParties, modulus);
    BigInteger macKey = getMacKeyFromSuppliers(suppliers);
    List<SpdzElement> bitShares = new ArrayList<>(noOfParties);
    for (SpdzConfigurableDataSupplier supplier : suppliers) {
      bitShares.add(supplier.getNextRandomFieldElement().value);
    }
    SpdzElement recombined = recombine(bitShares);
    assertMacCorrect(recombined, macKey, modulus);
    // sanity check not zero (with 251, that is actually not unlikely enough)
    if (!modulus.equals(new BigInteger("251"))) {
      BigInteger value = recombined.getShare();
      assertFalse("Random value was 0 ", value.equals(BigInteger.ZERO));
    }
  }

  private void testGetNextRandomFieldElement(int noOfParties) {
    for (BigInteger modulus : moduli) {
      testGetNextRandomFieldElement(noOfParties, modulus);
    }
  }

  @Test
  public void testGetNextTriple() {
    testGetNextTriple(2);
    testGetNextTriple(3);
    testGetNextTriple(5);
  }

  @Test
  public void testGetNextInputMask() {
    List<Integer> partyCounts = Arrays.asList(2, 3, 5);
    for (int partyCount : partyCounts) {
      for (int i = 0; i < partyCount; i++) {
        testGetNextInputMask(partyCount, i + 1);
      }
    }
  }

  @Test
  public void testGetNextBit() {
    testGetNextBit(2);
    testGetNextBit(3);
    testGetNextBit(5);
  }

  @Test
  public void testGetNextRandomFieldElement() {
    testGetNextRandomFieldElement(2);
    testGetNextRandomFieldElement(3);
    testGetNextRandomFieldElement(5);
  }

  private SpdzElement recombine(List<SpdzElement> shares) {
    return shares.stream().reduce(SpdzElement::add).get();
  }

  private SpdzTriple recombineTriples(List<SpdzTriple> triples) {
    List<SpdzElement> left = new ArrayList<>(triples.size());
    List<SpdzElement> right = new ArrayList<>(triples.size());
    List<SpdzElement> product = new ArrayList<>(triples.size());
    for (SpdzTriple triple : triples) {
      left.add(triple.getA());
      right.add(triple.getB());
      product.add(triple.getC());
    }
    return new SpdzTriple(recombine(left), recombine(right), recombine(product));
  }

  private void assertMacCorrect(SpdzElement recombined, BigInteger macKey, BigInteger modulus) {
    assertEquals(recombined.getShare().multiply(macKey).mod(modulus), recombined.getMac());
  }

  private void assertTripleValid(SpdzTriple recombined, BigInteger macKey, BigInteger modulus) {
    assertMacCorrect(recombined.getA(), macKey, modulus);
    assertMacCorrect(recombined.getB(), macKey, modulus);
    assertMacCorrect(recombined.getC(), macKey, modulus);
    // check that a * b = c
    assertEquals(recombined.getC().getShare(),
        recombined.getA().getShare().multiply(recombined.getB().getShare()).mod(modulus));
  }

}
