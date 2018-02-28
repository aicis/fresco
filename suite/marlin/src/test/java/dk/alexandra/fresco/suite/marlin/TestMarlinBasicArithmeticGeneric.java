package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDummyDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStoreImpl;
import java.util.function.Supplier;

public class TestMarlinBasicArithmeticGeneric extends
    MarlinTestSuite<MarlinResourcePool<GenericCompUInt>> {

  @Override
  protected MarlinResourcePool<GenericCompUInt> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<GenericCompUInt> factory = new GenericCompUIntFactory(32, 32);
    MarlinResourcePool<GenericCompUInt> resourcePool =
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
  protected ProtocolSuiteNumeric<MarlinResourcePool<GenericCompUInt>> createProtocolSuite() {
    return new MarlinProtocolSuiteGeneric(32, 32);
  }

}
