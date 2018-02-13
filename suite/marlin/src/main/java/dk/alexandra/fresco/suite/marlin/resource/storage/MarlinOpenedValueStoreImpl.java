package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import java.util.ArrayList;
import java.util.List;

public class MarlinOpenedValueStoreImpl<T extends BigUInt<T>> implements MarlinOpenedValueStore<T> {

  private final List<MarlinSInt<T>> sharesWithMacs;
  private final List<T> openedValues;

  public MarlinOpenedValueStoreImpl() {
    this.sharesWithMacs = new ArrayList<>();
    this.openedValues = new ArrayList<>();
  }

  @Override
  public void pushOpenedValues(List<MarlinSInt<T>> newSharesWithMacs, List<T> newOpenedValues) {
    sharesWithMacs.addAll(newSharesWithMacs);
    openedValues.addAll(newOpenedValues);
  }

  @Override
  public Pair<List<MarlinSInt<T>>, List<T>> popValues() {
    List<MarlinSInt<T>> macsToCheck = new ArrayList<>(sharesWithMacs);
    List<T> valuesToCheck = new ArrayList<>(openedValues);
    sharesWithMacs.clear();
    openedValues.clear();
    return new Pair<>(macsToCheck, valuesToCheck);
  }

  @Override
  public boolean isEmpty() {
    return sharesWithMacs.isEmpty();
  }

}
