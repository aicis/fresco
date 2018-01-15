package dk.alexandra.fresco.framework;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestMPCException {

  @Test
  public void testMessage() {
    MPCException ex = new MPCException("Message");
    Assert.assertThat(ex.getMessage(), Is.is("Message"));
  }
}

