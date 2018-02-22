package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import java.util.ArrayList;
import java.util.List;

public class MarlinOpenedValueStoreImpl<
    CompT extends CompUInt<?, ?, CompT>>
    implements MarlinOpenedValueStore<CompT> {

  private final List<MarlinSInt<CompT>> sharesWithMacs;
  private final List<CompT> openedValues;

  public MarlinOpenedValueStoreImpl() {
    this.sharesWithMacs = new ArrayList<>();
    this.openedValues = new ArrayList<>();
  }

  @Override
  public void pushOpenedValues(List<MarlinSInt<CompT>> newSharesWithMacs,
      List<CompT> newOpenedValues) {
    sharesWithMacs.addAll(newSharesWithMacs);
    openedValues.addAll(newOpenedValues);
  }

  @Override
  public Pair<List<MarlinSInt<CompT>>, List<CompT>> popValues() {
    List<MarlinSInt<CompT>> macsToCheck = new ArrayList<>(sharesWithMacs);
    List<CompT> valuesToCheck = new ArrayList<>(openedValues);
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
