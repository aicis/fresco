package dk.alexandra.fresco.suite.spdz2k.resource.storage;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import java.math.BigInteger;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdz2kOpenedValueStoreImpl {

  private final CompUInt128 open = new CompUInt128(BigInteger.ONE);
  private final Spdz2kSInt<CompUInt128> authenticated = new Spdz2kSInt<>(open, open);

  @Test
  public void testPushOpenedValues() {
    Spdz2kOpenedValueStore<CompUInt128> store = new Spdz2kOpenedValueStoreImpl<>();
    store.pushOpenedValue(authenticated, open);
    Assert.assertTrue("Store expected to have pending values", store.hasPendingValues());
    Assert.assertEquals(1, store.getNumPending());
    Assert.assertTrue(store.exceedsThreshold(0));
  }

  @Test
  public void testPeekValues() {
    Spdz2kOpenedValueStore<CompUInt128> store = new Spdz2kOpenedValueStoreImpl<>();
    store.pushOpenedValue(authenticated, open);
    store.pushOpenedValue(authenticated, open);
    Pair<List<Spdz2kSInt<CompUInt128>>, List<CompUInt128>> both = store.peekValues();
    List<Spdz2kSInt<CompUInt128>> first = both.getFirst();
    Assert.assertEquals(2, first.size());
    List<CompUInt128> second = both.getSecond();
    Assert.assertEquals(2, second.size());
    Assert.assertEquals(authenticated, first.get(0));
    Assert.assertEquals(authenticated, first.get(1));
    Assert.assertEquals(open, second.get(0));
    Assert.assertEquals(open, second.get(1));
    Assert.assertFalse("Store not expected to have pending values", store.hasPendingValues());
    store.clear();
  }

  @Test(expected = IllegalStateException.class)
  public void testClearBeforeAllChecked() {
    Spdz2kOpenedValueStore<CompUInt128> store = new Spdz2kOpenedValueStoreImpl<>();
    store.pushOpenedValue(authenticated, open);
    store.clear();
  }

}
