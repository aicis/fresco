package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests miscellaneous classes such as util and resource pool
 */
public class TestSpdzMisc {

  @Test
  public void testPreproStrat() {
    for (PreprocessingStrategy pps : PreprocessingStrategy.values()) {
      PreprocessingStrategy strat = PreprocessingStrategy.valueOf(pps.name());
      Assert.assertEquals(pps, strat);
    }
  }

  @Test(expected = RuntimeException.class)
  public void testResourcePoolStoreNotInitialized() {
    SpdzStorage store = new SpdzStorageImpl(
        new SpdzStorageDataSupplier(new FilebasedStreamedStorageImpl(new InMemoryStorage()), "null",
            2));
    new SpdzResourcePoolImpl(1, 2, store);
  }

  @Test(expected = IllegalStateException.class)
  public void testSpdzExponentiationPipeProtocolExpPipeFailedLength() {
    SpdzStorage store = new SpdzStorageImpl(new SpdzDummyDataSupplier(1, 2, new BigInteger("251")));
    SpdzResourcePool rp = new SpdzResourcePoolImpl(1, 2, store);
    SpdzExponentiationPipeProtocol pro = new SpdzExponentiationPipeProtocol(
        FakeTripGen.EXP_PIPE_SIZE);
    pro.evaluate(0, rp, null);
  }
}
