package dk.alexandra.fresco.framework.util;

import org.junit.Test;

public class ExceptionConverterTest {

  @Test
  public void construct() throws Exception {
    new ExceptionConverter();
  }

  @Test(expected = RuntimeException.class)
  public void safeCompute() throws Exception {
    ExceptionConverter.safe(
        () -> {
          throw new NullPointerException();
        },
        "Ignored");
  }

  @Test(expected = RuntimeException.class)
  public void safeComputeWithExceptionHandling() throws Exception {
    ExceptionConverter.safe(
        () -> {
          throw new NullPointerException();
        },
        () -> {
          throw new NullPointerException();
        },
        "Ignored");
  }

}