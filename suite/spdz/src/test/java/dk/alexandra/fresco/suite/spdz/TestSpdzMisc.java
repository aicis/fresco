package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplierImpl;
import dk.alexandra.fresco.suite.spdz.storage.DummyDataSupplierImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorageImpl;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import java.security.NoSuchAlgorithmException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests miscellaneous classes such as util and resource pool
 *
 */
public class TestSpdzMisc {

  @Test
  public void testUtil() {
    Util util = new Util();
    Assert.assertNotNull(util);
  }
  
  @Test
  public void testPreproStrat() {
    for(PreprocessingStrategy pps : PreprocessingStrategy.values()) {
      PreprocessingStrategy strat = PreprocessingStrategy.valueOf(pps.name());
      Assert.assertEquals(pps, strat);
    }
  }
  
  @Test(expected=MPCException.class)
  public void testResourcePoolStoreNotInitialized() throws NoSuchAlgorithmException {
    SpdzStorage store = new SpdzStorageImpl(new DataSupplierImpl(new FilebasedStreamedStorageImpl(new InMemoryStorage()), "null", 2));
    new SpdzResourcePoolImpl(1, 2, null, null, store);
  }
  
  @Test(expected=MPCException.class)
  public void testSpdzExponentiationPipeProtocolExpPipeFailedLength() throws NoSuchAlgorithmException {
    SpdzStorage store = new SpdzStorageImpl(new DummyDataSupplierImpl(1, 2));
    SpdzResourcePool rp = new SpdzResourcePoolImpl(1, 2, null, null, store);
    SpdzExponentiationPipeProtocol pro = new SpdzExponentiationPipeProtocol(Util.EXP_PIPE_SIZE);
    pro.evaluate(0, rp, null);
  }
}
