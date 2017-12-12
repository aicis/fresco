package dk.alexandra.fresco.framework.util;

import java.util.concurrent.Callable;

public class ExceptionConverter {

  ExceptionConverter() {
  }

  public static <T> T safe(Callable<T> callable, String message) {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(message, e);
    }
  }
}
