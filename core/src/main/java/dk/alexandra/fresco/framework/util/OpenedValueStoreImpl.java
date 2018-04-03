package dk.alexandra.fresco.framework.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements {@link OpenedValueStore}.
 */
public class OpenedValueStoreImpl<AuthT, OpenT> implements OpenedValueStore<AuthT, OpenT> {

  private List<AuthT> sharesWithMacs;
  private List<OpenT> openValues;

  public OpenedValueStoreImpl() {
    this.sharesWithMacs = new ArrayList<>();
    this.openValues = new ArrayList<>();
  }

  @Override
  public void pushOpenedValues(List<AuthT> newSharesWithMacs, List<OpenT> newOpenedValues) {
    sharesWithMacs.addAll(newSharesWithMacs);
    openValues.addAll(newOpenedValues);
  }

  @Override
  public Pair<List<AuthT>, List<OpenT>> popValues() {
    // the caller has the responsibility of managing the returned lists, so we clear by referencing
    // new empty lists (which avoids copying) and return references to original value lists
    List<AuthT> tempSharesWithMacs = sharesWithMacs;
    List<OpenT> tempOpenedValues = openValues;
    sharesWithMacs = new ArrayList<>();
    openValues = new ArrayList<>();
    return new Pair<>(tempSharesWithMacs, tempOpenedValues);
  }

  @Override
  public boolean hasPendingValues() {
    return sharesWithMacs.size() > 0;
  }

  @Override
  public boolean exceedsThreshold(int threshold) {
    return sharesWithMacs.size() > threshold;
  }

}
