package dk.alexandra.fresco.lib.common.math.util;

import static org.mockito.Mockito.mock;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.lib.common.util.RowPair;
import org.junit.Test;

public class RowPairTest {

  @Test
  public void testConstructor() {
    // Test that it doesn't break upon construction
    RowPair constructed = new RowPair<>(mock(DRes.class), mock(DRes.class));
  }
}
