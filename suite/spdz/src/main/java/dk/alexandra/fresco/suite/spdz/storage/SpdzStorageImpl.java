package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * Uses the D14.2 storage concept as backend
 *
 * @author Kasper Damgaard
 */
public class SpdzStorageImpl implements SpdzStorage {

  private List<BigInteger> openedValues;
  private List<SpdzElement> closedValues;

  private SpdzDataSupplier supplier;

  /**
   * Creates an instance of the SpdzStorageImpl class using the given data supplier. 
   * @param supplier The way the storage should provide and get data. 
   */
  public SpdzStorageImpl(SpdzDataSupplier supplier) {
    this.supplier = supplier;
    openedValues = new LinkedList<>();
    closedValues = new LinkedList<>();
  }

  @Override
  public void reset() {
    openedValues.clear();
    closedValues.clear();
  }

  @Override
  public SpdzDataSupplier getSupplier() {
    return this.supplier;
  }

  @Override
  public void addOpenedValue(BigInteger val) {
    openedValues.add(val);
  }

  @Override
  public void addClosedValue(SpdzElement elem) {
    closedValues.add(elem);
  }

  @Override
  public List<BigInteger> getOpenedValues() {
    return openedValues;
  }

  @Override
  public List<SpdzElement> getClosedValues() {
    return closedValues;
  }

  @Override
  public BigInteger getSecretSharedKey() {
    return this.supplier.getSecretSharedKey();
  }

}
