package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import java.util.ArrayList;
import java.util.List;

public class Spdz2kOpenedValueStoreImpl<PlainT extends CompUInt<?, ?, PlainT>>
    implements Spdz2kOpenedValueStore<PlainT> {

  private final List<Spdz2kSInt<PlainT>> sharesWithMacs;
  private final List<PlainT> openedValues;

  public Spdz2kOpenedValueStoreImpl() {
    this.sharesWithMacs = new ArrayList<>();
    this.openedValues = new ArrayList<>();
  }

  @Override
  public void pushOpenedValues(List<Spdz2kSInt<PlainT>> newSharesWithMacs,
      List<PlainT> newOpenedValues) {
    sharesWithMacs.addAll(newSharesWithMacs);
    openedValues.addAll(newOpenedValues);
  }

  @Override
  public Pair<List<Spdz2kSInt<PlainT>>, List<PlainT>> popValues() {
    List<Spdz2kSInt<PlainT>> macsToCheck = new ArrayList<>(sharesWithMacs);
    List<PlainT> valuesToCheck = new ArrayList<>(openedValues);
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
