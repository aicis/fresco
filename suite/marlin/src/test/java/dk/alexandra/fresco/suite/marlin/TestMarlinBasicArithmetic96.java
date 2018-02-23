package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt96Factory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDummyDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStoreImpl;
import java.util.function.Supplier;

public class TestMarlinBasicArithmetic96 extends MarlinTestSuite<MarlinResourcePool<CompUInt96>> {

  @Override
  protected MarlinResourcePool<CompUInt96> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<CompUInt96> factory = new CompUInt96Factory();
    MarlinResourcePool<CompUInt96> resourcePool =
        new MarlinResourcePoolImpl<>(
            playerId,
            noOfParties, null,
            new MarlinOpenedValueStoreImpl<>(),
            new MarlinDummyDataSupplier<>(playerId, noOfParties, factory.createRandom(), factory),
            factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected ProtocolSuiteNumeric<MarlinResourcePool<CompUInt96>> createProtocolSuite() {
    return new MarlinProtocolSuite96();
  }

}
