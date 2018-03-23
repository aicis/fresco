package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import org.junit.Test;

public class TestBroadcastValidationProtocol {

  @Test(expected = IllegalStateException.class)
  public void testOut() {
    new BroadcastValidationProtocol<>(new byte[]{}).out();
  }

}
