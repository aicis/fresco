package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96Factory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter96;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt32;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.util.function.Supplier;

public class TestMarlinBasicArithmetic96 extends MarlinTestSuite<
    UInt64,
    UInt32,
    CompUInt96,
    MarlinResourcePool<CompUInt96>> {

  @Override
  protected MarlinResourcePool<CompUInt96> createResourcePool(int playerId,
      int noOfParties, MarlinOpenedValueStore<CompUInt96> store,
      MarlinDataSupplier<CompUInt96> supplier,
      CompUIntFactory<CompUInt96> factory, Supplier<Network> networkSupplier) {
    MarlinResourcePool<CompUInt96> resourcePool =
        new MarlinResourcePoolImpl<>(
            playerId,
            noOfParties, null, store, supplier, factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected CompUIntFactory<CompUInt96> createFactory() {
    return new CompUInt96Factory();
  }

  @Override
  protected ProtocolSuiteNumeric<MarlinResourcePool<CompUInt96>> createProtocolSuite() {
    return new MarlinProtocolSuite<>(new CompUIntConverter96());
  }

}
