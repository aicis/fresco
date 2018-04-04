package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestOpenedValueStoreImpl {

  private final BigInteger open = BigInteger.ZERO;
  private final BigInteger authenticated = BigInteger.ONE;

  @Test
  public void testPushOpenedValues() {
    OpenedValueStore<BigInteger, BigInteger> store = new OpenedValueStoreImpl<>();
    store.pushOpenedValue(authenticated, open);
    Assert.assertTrue("Store expected to have pending values", store.hasPendingValues());
    Assert.assertTrue(store.exceedsThreshold(0));
    Assert.assertFalse(store.exceedsThreshold(1));
  }

  @Test
  public void testPopValues() {
    OpenedValueStore<BigInteger, BigInteger> store = new OpenedValueStoreImpl<>();
    store.pushOpenedValue(authenticated, open);
    store.pushOpenedValue(authenticated, open);
    Pair<List<BigInteger>, List<BigInteger>> both = store.popValues();
    List<BigInteger> first = both.getFirst();
    Assert.assertEquals(2, first.size());
    List<BigInteger> second = both.getSecond();
    Assert.assertEquals(2, second.size());
    Assert.assertEquals(authenticated, first.get(0));
    Assert.assertEquals(authenticated, first.get(1));
    Assert.assertEquals(open, second.get(0));
    Assert.assertEquals(open, second.get(1));
    Assert.assertFalse("Store not expected to have pending values", store.hasPendingValues());
  }

  @Test
  public void testPopTwice() {
    OpenedValueStore<BigInteger, BigInteger> store = new OpenedValueStoreImpl<>();
    store.pushOpenedValues(Collections.singletonList(authenticated),
        Collections.singletonList(open));
    Pair<List<BigInteger>, List<BigInteger>> both = store.popValues();
    Pair<List<BigInteger>, List<BigInteger>> actual = store.popValues();
    Assert.assertTrue("Second pop statement should return empty list", actual.getFirst().isEmpty());
    // check that value retrieved through the first call are not affected by second call
    List<BigInteger> first = both.getFirst();
    Assert.assertEquals(1, first.size());
    List<BigInteger> second = both.getSecond();
    Assert.assertEquals(1, second.size());
    Assert.assertEquals(authenticated, first.get(0));
    Assert.assertEquals(open, second.get(0));
  }

}
