package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.GenericCompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.GenericCompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStoreImpl;
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
