package dk.alexandra.fresco.suite.spdz2k.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import java.util.ArrayList;
import java.util.List;

public class Spdz2kOpenedValueStoreImpl<PlainT extends CompUInt<?, ?, PlainT>>
    implements Spdz2kOpenedValueStore<PlainT> {

  private final List<Spdz2kSInt<PlainT>> sharesWithMacs;
  private final List<PlainT> openedValues;
  private int numPending;

  public Spdz2kOpenedValueStoreImpl() {
    this.sharesWithMacs = new ArrayList<>();
    this.openedValues = new ArrayList<>();
    numPending = 0;
  }

  @Override
  public void pushOpenedValues(List<Spdz2kSInt<PlainT>> newSharesWithMacs,
      List<PlainT> newOpenedValues) {
    sharesWithMacs.addAll(newSharesWithMacs);
    openedValues.addAll(newOpenedValues);
    numPending += newSharesWithMacs.size();
  }

  @Override
  public Pair<List<Spdz2kSInt<PlainT>>, List<PlainT>> peekValues() {
    numPending -= sharesWithMacs.size();
    return new Pair<>(sharesWithMacs, openedValues);
  }

  @Override
  public void clear() {
    if (numPending > 0) {
      throw new IllegalStateException("Called clear before checking all values");
    }
    sharesWithMacs.clear();
    openedValues.clear();
  }

  @Override
  public boolean hasPendingValues() {
    return numPending == 0;
  }

  @Override
  public int size() {
    return sharesWithMacs.size();
  }

  @Override
  public boolean exceedsThreshold(int threshold) {
    return numPending > threshold;
  }

}
