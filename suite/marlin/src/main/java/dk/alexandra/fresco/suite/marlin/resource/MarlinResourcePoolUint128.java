package dk.alexandra.fresco.suite.marlin.resource;

import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;

public class MarlinResourcePoolUint128 extends MarlinResourcePoolImpl<UInt64, UInt64, UInt128> {

  /**
   * Creates new {@link MarlinResourcePool} for the 128 bit case.
   */
  public MarlinResourcePoolUint128(int myId, int noOfPlayers,
      Drbg drbg,
      MarlinOpenedValueStore<UInt64, UInt64, UInt128> storage,
      MarlinDataSupplier<UInt64, UInt64, UInt128> supplier,
      CompUIntFactory<UInt64, UInt64, UInt128> factory) {
    super(myId, noOfPlayers, drbg, storage, supplier, factory);
  }
}
