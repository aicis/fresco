package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt64;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt64Factory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStoreImpl;
import java.util.function.Supplier;

public class TestSpdz2k64 extends Spdz2kTestSuite<Spdz2kResourcePool<CompUInt64>> {

  @Override
  protected Spdz2kResourcePool<CompUInt64> createResourcePool(int playerId, int noOfParties,
      Supplier<Network> networkSupplier) {
    CompUIntFactory<CompUInt64> factory = new CompUInt64Factory();
    Spdz2kResourcePool<CompUInt64> resourcePool =
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
  protected ProtocolSuiteNumeric<Spdz2kResourcePool<CompUInt64>> createProtocolSuite() {
    return new Spdz2kProtocolSuite64(true);
  }

  // TODO implement fixed-point arithmetic

  @Override
  public void testRealInput() {
  }

  @Override
  public void testRealOpenToParty() {
  }

  @Override
  public void testRealKnown() {
  }

  @Override
  public void test_Real_Add_Secret() {
  }

  @Override
  public void test_Real_Mult_Known() {
  }

  @Override
  public void test_Real_Mults() {
  }

  @Override
  public void test_Real_Mults_Isolated() {
  }

  @Override
  protected int getMaxBitLength() {
    return 32;
  }

}
