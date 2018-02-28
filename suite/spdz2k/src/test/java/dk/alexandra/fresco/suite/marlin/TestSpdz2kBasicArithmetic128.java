package dk.alexandra.fresco.suite.marlin;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt128Factory;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kOpenedValueStoreImpl;
import java.util.function.Supplier;

public class TestSpdz2kBasicArithmetic128 extends Spdz2kTestSuite<Spdz2kResourcePool<CompUInt128>> {

  @Override
  protected Spdz2kResourcePool<CompUInt128> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<CompUInt128> factory = new CompUInt128Factory();
    Spdz2kResourcePool<CompUInt128> resourcePool =
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
  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt128>> createProtocolSuite() {
    return new Spdz2kProtocolSuite128();
  }

}
