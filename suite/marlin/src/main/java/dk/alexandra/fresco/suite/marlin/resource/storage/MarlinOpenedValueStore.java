package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import java.util.Collections;
import java.util.List;

/**
 * A class that stores all opened values along with their macs for subsequent mac checks.
 */
public interface MarlinOpenedValueStore<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> {

  /**
   * Store elements with macs that were just opened along with the corresponding open values.
   */
  void pushOpenedValues(List<MarlinSInt<H, L, T>> newSharesWithMacs, List<T> newOpenedValues);

  default void pushOpenedValue(MarlinSInt<H, L, T> newShareWithMac, T newOpenedValue) {
    pushOpenedValues(Collections.singletonList(newShareWithMac),
        Collections.singletonList(newOpenedValue));
  }

  /**
   * Retrieve all values that haven't been checked yet and clear the store.
   */
  Pair<List<MarlinSInt<H, L, T>>, List<T>> popValues();

  /**
   * Check if there are unchecked values.
   */
  boolean isEmpty();

  /**
   * Check number of unchecked values.
   */
  int size();

}
