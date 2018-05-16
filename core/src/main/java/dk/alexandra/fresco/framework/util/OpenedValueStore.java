package dk.alexandra.fresco.framework.util;

import java.util.Collections;
import java.util.List;

/**
 * Generic functionality for storing authenticated values and their corresponding open values for a
 * future mac-check. <p>This interface can be used by actively-secure protocol suites such as the
 * SPDZ suite that allow for accumulating open values before performing a batched mac check.</p>
 *
 * @param <AuthT> the type of the authenticated element, i.e., the element holding the mac-share
 * @param <OpenT> the type of the opened element
 */
public interface OpenedValueStore<AuthT, OpenT> {

  /**
   * Store elements with macs that were just opened along with the corresponding open values.
   *
   * @param newSharesWithMacs the authenticated values storing the mac shares
   * @param newOpenedValues the open values
   */
  void pushOpenedValues(List<AuthT> newSharesWithMacs, List<OpenT> newOpenedValues);

  /**
   * Default call to {@link #pushOpenedValues(List, List)} that wraps single elements in lists.
   *
   * @param newShareWithMac the authenticated element storing the mac share
   * @param newOpenedValue the open value
   */
  default void pushOpenedValue(AuthT newShareWithMac, OpenT newOpenedValue) {
    pushOpenedValues(Collections.singletonList(newShareWithMac),
        Collections.singletonList(newOpenedValue));
  }

  /**
   * Retrieve all values that haven't been checked yet. <p>Note that this passes ownership of the
   * underlying lists to the caller. The caller is responsible for managing and clearing the
   * lists.</p>
   *
   * @return a pair of lists, where the first list contains the authenticated elements scheduled for
   * checking and the second list contains the open values
   */
  Pair<List<AuthT>, List<OpenT>> popValues();

  /**
   * Check if there are unchecked values.
   *
   * @return true if all values have been retrieved via {@link #popValues()} and false if there are
   * pending values
   */
  boolean hasPendingValues();

  /**
   * Check if number of unchecked values exceeds given threshold.
   *
   * @return true if the number of not yet retrieved values is greater than the threshold and false
   * otherwise
   */
  boolean exceedsThreshold(int threshold);

}
