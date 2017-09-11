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

  @Test
  public void testMessageAndException() {
    Exception e = new Exception("Parent");
    MPCException ex = new MPCException("Message", e);
    Assert.assertThat(ex.getMessage(), Is.is("Message"));
    Assert.assertThat(ex.getCause(), Is.is(e));
  }
  
}

