package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.GenericCompUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kOpenedValueStoreImpl;
import java.util.function.Supplier;

public class TestSpdz2kBasicArithmeticGeneric extends
    Spdz2kTestSuite<Spdz2kResourcePool<GenericCompUInt>> {

  @Override
  protected Spdz2kResourcePool<GenericCompUInt> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<GenericCompUInt> factory = new GenericCompUIntFactory(32, 32);
    Spdz2kResourcePool<GenericCompUInt> resourcePool =
        new Spdz2kResourcePoolImpl<>(
            playerId,
            noOfParties, null,
            new Spdz2kOpenedValueStoreImpl<>(),
            new Spdz2kDummyDataSupplier<>(playerId, noOfParties, factory.createRandom(), factory),
            factory);
    resourcePool.initializeJointRandomness(networkSupplier, AesCtrDrbg::new, 32);
    return resourcePool;
  }

  @Override
  protected ProtocolSuiteNumeric<Spdz2kResourcePool<GenericCompUInt>> createProtocolSuite() {
    return new Spdz2kProtocolSuiteGeneric(32, 32);
  }

}
