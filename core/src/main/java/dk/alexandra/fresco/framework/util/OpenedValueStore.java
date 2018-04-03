package dk.alexandra.fresco.framework.util;

import java.util.Collections;
import java.util.List;

/**
 * Generic functionality for storing authenticated values and their corresponding open values for a
 * future mac-check. <p>This interface can be used by actively-secure protocol suites such as the
 * SPDZ suite that allow for accumulating open values before performing a batched mac check.</p>
 */
public interface OpenedValueStore<AuthT, OpenT> {

  /**
   * Store elements with macs that were just opened along with the corresponding open values.
   */
  void pushOpenedValues(List<AuthT> newSharesWithMacs, List<OpenT> newOpenedValues);

  /**
   * Default call to {@link #pushOpenedValues(List, List)} that wraps single elements in lists.
   */
  default void pushOpenedValue(AuthT newShareWithMac, OpenT newOpenedValue) {
    pushOpenedValues(Collections.singletonList(newShareWithMac),
        Collections.singletonList(newOpenedValue));
  }

  /**
   * Retrieve all values that haven't been checked yet. <p>Note that this passes ownership of the
   * underlying lists to the caller. The caller is responsible for managing and clearing the
   * lists.</p>
   */
  Pair<List<AuthT>, List<OpenT>> popValues();

  /**
   * Check if there are unchecked values.
   */
  boolean hasPendingValues();

  /**
   * Check if number of unchecked values exceeds given threshold.
   */
  boolean exceedsThreshold(int threshold);

}
