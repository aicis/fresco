package dk.alexandra.fresco.suite.marlin.storage;

import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import java.util.List;

public interface MarlinStorage<T extends BigUInt<T>> {

  /**
   * Resets the opened and closed values.
   */
  void reset();

  /**
   * Gets a data supplier for supplying preprocessed data values. TODO does this really go here
   *
   * @return a data supplier
   */
  MarlinDataSupplier<T> getSupplier();

  /**
   * Adds an opened value.
   *
   * @param val a value to be added
   */
  void addOpenedValue(T val);

  /**
   * Adds a closed values.
   *
   * @param elem a element to add
   */
  void addClosedValue(MarlinElement<T> elem);

  /**
   * Get the current opened values.
   *
   * @return a list of opened values
   */
  List<T> getOpenedValues();

  /**
   * Get the current closed values.
   *
   * @return a list of closed values
   */
  List<MarlinElement<T>> getClosedValues();

  /**
   * Returns the players share of the Secret Shared Key (alpha).
   *
   * @return alpha_i
   */
  T getSecretSharedKey();

}
