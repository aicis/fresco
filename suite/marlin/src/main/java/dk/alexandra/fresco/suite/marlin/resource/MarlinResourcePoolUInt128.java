package dk.alexandra.fresco.suite.marlin.resource;

import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;

public class MarlinResourcePoolUInt128 extends MarlinResourcePoolImpl<UInt64, UInt64, CompUInt128> {

  /**
   * Creates new {@link MarlinResourcePool} for the 128 bit case.
   */
  public MarlinResourcePoolUInt128(int myId, int noOfPlayers,
      Drbg drbg,
      MarlinOpenedValueStore<CompUInt128> storage,
      MarlinDataSupplier<CompUInt128> supplier,
      CompUIntFactory<UInt64, UInt64, CompUInt128> factory) {
    super(myId, noOfPlayers, drbg, storage, supplier, factory);
  }

}
