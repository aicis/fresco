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

  private List<BigInteger> opened_values;
  private List<SpdzElement> closed_values;

  private DataSupplier supplier;

  /**
   * Creates an instance of the SpdzStorageImpl class using the given data supplier. 
   * @param supplier The way the storage should provide and get data. 
   */
  public SpdzStorageImpl(DataSupplier supplier) {
    this.supplier = supplier;
    opened_values = new LinkedList<>();
    closed_values = new LinkedList<>();
  }

  @Override
  public void reset() {
    opened_values.clear();
    closed_values.clear();
  }

  @Override
  public DataSupplier getSupplier() {
    return this.supplier;
  }

  @Override
  public void addOpenedValue(BigInteger val) {
    opened_values.add(val);
  }

  @Override
  public void addClosedValue(SpdzElement elem) {
    closed_values.add(elem);
  }

  @Override
  public List<BigInteger> getOpenedValues() {
    return opened_values;
  }

  @Override
  public List<SpdzElement> getClosedValues() {
    return closed_values;
  }

  @Override
  public BigInteger getSSK() {
    return this.supplier.getSSK();
  }

}
