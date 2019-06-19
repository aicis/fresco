package dk.alexandra.fresco.lib.generic;

import org.junit.Test;

public class BroadcastValidationProtocolTest {

  @Test(expected = IllegalStateException.class)
  public void testOutThrows() {
    new BroadcastValidationProtocol(new byte[1]).out();
  }
}
