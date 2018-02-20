package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import java.util.ArrayList;
import java.util.List;

public class MarlinOpenedValueStoreImpl<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> implements
    MarlinOpenedValueStore<H, L, T> {

  private final List<MarlinSInt<H, L, T>> sharesWithMacs;
  private final List<T> openedValues;

  public MarlinOpenedValueStoreImpl() {
    this.sharesWithMacs = new ArrayList<>();
    this.openedValues = new ArrayList<>();
  }

  @Override
  public void pushOpenedValues(List<MarlinSInt<H, L, T>> newSharesWithMacs, List<T> newOpenedValues) {
    sharesWithMacs.addAll(newSharesWithMacs);
    openedValues.addAll(newOpenedValues);
  }

  @Override
  public Pair<List<MarlinSInt<H, L, T>>, List<T>> popValues() {
    List<MarlinSInt<H, L, T>> macsToCheck = new ArrayList<>(sharesWithMacs);
    List<T> valuesToCheck = new ArrayList<>(openedValues);
    sharesWithMacs.clear();
    openedValues.clear();
    return new Pair<>(macsToCheck, valuesToCheck);
  }

  @Override
  public boolean isEmpty() {
    return sharesWithMacs.isEmpty();
  }

  @Override
  public int size() {
    return sharesWithMacs.size();
  }

}
