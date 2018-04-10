package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.lib.broadcast.BroadcastValidationProtocol;
import org.junit.Test;

public class TestBroadcastValidationProtocol {

  @Test(expected = IllegalStateException.class)
  public void testOut() {
    new BroadcastValidationProtocol<>(new byte[]{}).out();
  }

}
