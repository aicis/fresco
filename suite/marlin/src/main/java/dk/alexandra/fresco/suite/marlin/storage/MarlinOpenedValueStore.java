package dk.alexandra.fresco.suite.marlin.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import java.util.List;

/**
 * A class that stores all opened values along with their macs for subsequent mac checks.
 */
public interface MarlinOpenedValueStore<T extends BigUInt<T>> {

  /**
   * Store elements with macs that were just opened along with the corresponding open values.
   */
  void pushOpenedValues(List<MarlinElement<T>> newSharesWithMacs, List<T> newOpenedValues);

  /**
   * Retrieves all values that haven't been checked yet and clears the store.
   */
  Pair<List<MarlinElement<T>>, List<T>> popValues();

}
