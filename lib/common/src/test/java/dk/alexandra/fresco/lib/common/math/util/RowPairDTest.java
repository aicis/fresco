package dk.alexandra.fresco.lib.common.math.util;

import static org.mockito.Mockito.mock;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.lib.common.util.RowPairD;
import org.junit.Test;

public class RowPairDTest {

  @Test
  public void testConstructor() {
    // Test that it doesn't break upon construction
    RowPairD constructed = new RowPairD<>(mock(DRes.class), mock(DRes.class));
  }
}
