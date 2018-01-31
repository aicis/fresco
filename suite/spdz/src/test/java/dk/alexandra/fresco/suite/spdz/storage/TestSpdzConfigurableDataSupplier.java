package dk.alexandra.fresco.suite.spdz.storage;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
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

  private void testGetNextTriple(int noOfParties, BigInteger modulus) {
    List<SpdzConfigurableDataSupplier> suppliers = new ArrayList<>(noOfParties);
    List<BigInteger> macKeyShares = new ArrayList<>(noOfParties);
    Random random = new Random();
    for (int i = 0; i < noOfParties; i++) {
      BigInteger macKeyShare = new BigInteger(modulus.bitLength(), random).mod(modulus);
      macKeyShares.add(macKeyShare);
      suppliers.add(new SpdzConfigurableDataSupplier(i + 1, noOfParties, modulus, macKeyShare));
    }
    List<SpdzTriple> triples = new ArrayList<>(noOfParties);
    for (SpdzConfigurableDataSupplier supplier : suppliers) {
      triples.add(supplier.getNextTriple());
    }
    SpdzTriple recombined = recombineTriples(triples);
    assertTripleValid(recombined, macKeyShares, modulus);
  }

  private void testGetNextTriple(int noOfParties) {
    for (BigInteger modulus : moduli) {
      testGetNextTriple(noOfParties, modulus);
    }
  }

  @Test
  public void testGetNextTriple() {
    testGetNextTriple(2);
    testGetNextTriple(3);
    testGetNextTriple(5);
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

  private void assertTripleValid(SpdzTriple recombined, List<BigInteger> macKeyShares,
      BigInteger modulus) {
    BigInteger macKey = MathUtils.sum(macKeyShares, modulus);
    assertMacCorrect(recombined.getA(), macKey, modulus);
    assertMacCorrect(recombined.getB(), macKey, modulus);
    assertMacCorrect(recombined.getC(), macKey, modulus);
    // check that a * b = c
    assertEquals(recombined.getC().getShare(),
        recombined.getA().getShare().multiply(recombined.getB().getShare()).mod(modulus));
  }

}
