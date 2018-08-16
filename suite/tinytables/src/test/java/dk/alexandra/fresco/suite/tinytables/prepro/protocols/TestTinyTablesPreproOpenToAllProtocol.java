package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TestTinyTablesPreproOpenToAllProtocol {

  @Test
  public void testOut() {
    assertNull((new TinyTablesPreproOpenToAllProtocol(0, null)).out());
  }

}
