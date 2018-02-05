package dk.alexandra.fresco.suite.marlin.storage;

import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import java.util.List;

public class MarlinStorageImpl<T extends BigUInt<T>> implements MarlinStorage<T> {

  @Override
  public void reset() {

  }

  @Override
  public MarlinDataSupplier<T> getSupplier() {
    return null;
  }

  @Override
  public void addOpenedValue(T val) {

  }

  @Override
  public void addClosedValue(MarlinElement<T> elem) {

  }

  @Override
  public List<T> getOpenedValues() {
    return null;
  }

  @Override
  public List<MarlinElement<T>> getClosedValues() {
    return null;
  }

  @Override
  public T getSecretSharedKey() {
    return null;
  }

}
