package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import java.util.Collections;
import java.util.List;

/**
 * A class that stores all opened values along with their macs for subsequent mac checks.
 */
public interface Spdz2kOpenedValueStore<T extends CompUInt<?, ?, T>> {

  /**
   * Store elements with macs that were just opened along with the corresponding open values.
   */
  void pushOpenedValues(List<Spdz2kSInt<T>> newSharesWithMacs, List<T> newOpenedValues);

  default void pushOpenedValue(Spdz2kSInt<T> newShareWithMac, T newOpenedValue) {
    pushOpenedValues(Collections.singletonList(newShareWithMac),
        Collections.singletonList(newOpenedValue));
  }

  /**
   * Retrieve all values that haven't been checked yet and clear the store.
   */
  Pair<List<Spdz2kSInt<T>>, List<T>> popValues();

  /**
   * Check if there are unchecked values.
   */
  boolean isEmpty();

  /**
   * Check number of unchecked values.
   */
  int size();

}
