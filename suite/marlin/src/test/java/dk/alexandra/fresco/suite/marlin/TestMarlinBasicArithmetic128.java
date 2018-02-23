package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter128;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt64;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.util.function.Supplier;

public class TestMarlinBasicArithmetic128 extends MarlinTestSuite<
    UInt64,
    UInt64,
    CompUInt128,
    MarlinResourcePool<UInt64, UInt64, CompUInt128>> {

  @Override
  protected MarlinResourcePool<UInt64, UInt64, CompUInt128> createResourcePool(int playerId,
      int noOfParties, MarlinOpenedValueStore<CompUInt128> store,
      MarlinDataSupplier<CompUInt128> supplier,
      CompUIntFactory<CompUInt128> factory, Supplier<Network> networkSupplier) {
    MarlinResourcePool<UInt64, UInt64, CompUInt128> resourcePool =
        new MarlinResourcePoolImpl<>(
            playerId,
            noOfParties, null, store, supplier, factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected CompUIntFactory<CompUInt128> createFactory() {
    return new CompUInt128Factory();
  }

  @Override
  protected ProtocolSuiteNumeric<MarlinResourcePool<UInt64, UInt64, CompUInt128>> createProtocolSuite() {
    return new MarlinProtocolSuite<>(new CompUIntConverter128());
  }


}
