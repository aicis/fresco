package dk.alexandra.fresco.suite.spdz2k.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
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
   * Retrieve all values that haven't been checked yet. <p>Note that this does <b>not</b> clear the
   * values. It only only sets are flag that these values have been scheduled for clearing. Once
   * mac-check is done, use {@link #clear()} for clearing.</p>
   */
  Pair<List<Spdz2kSInt<T>>, List<T>> peekValues();

  /**
   * Clear all values after checking.
   */
  void clear();

  /**
   * Check if there are unchecked values.
   */
  boolean hasPendingValues();

  /**
   * Check number of unchecked values.
   */
  int getNumPending();

  /**
   * Check if number of values not yet scheduled for checking exceeds given threshold.
   */
  boolean exceedsThreshold(int threshold);

}
