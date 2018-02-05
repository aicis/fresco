package dk.alexandra.fresco.suite.marlin.storage;

import static org.junit.Assert.assertArrayEquals;

import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import dk.alexandra.fresco.suite.marlin.datatypes.MutableUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.MutableUInt128Factory;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestMarlinDummyDataSupplier {

  private List<MarlinDataSupplier<MutableUInt128>> setupSupplier(int noOfParties) {
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

  private void testGetNextRandomElement(int noOfParties) {
    List<MarlinDataSupplier<MutableUInt128>> suppliers = setupSupplier(noOfParties);
    MutableUInt128 macKey = getMacKeyFromSuppliers(suppliers);
    List<MarlinElement<MutableUInt128>> shares = new ArrayList<>(noOfParties);
    for (MarlinDataSupplier<MutableUInt128> supplier : suppliers) {
      shares.add(supplier.getNextRandomElement().getValue());
    }
    MarlinElement<MutableUInt128> recombined = recombine(shares);
    assertMacCorrect(recombined, macKey);
  }

  @Test
  public void testGetNextRandomElement() {
    testGetNextRandomElement(2);
    testGetNextRandomElement(3);
    testGetNextRandomElement(5);
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


}
