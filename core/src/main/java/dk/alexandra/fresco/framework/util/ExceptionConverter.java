package dk.alexandra.fresco.framework.util;

import java.util.concurrent.Callable;

/**
 * This class converts typed exceptions to runtime exceptions.
 * Use with care, only typed exceptions hould be converted.
 */
public class ExceptionConverter {

  ExceptionConverter() {
  }

  /**
   * Use this method to create a safe call.
   *
   * @param callable the method to execute
   * @param message the error message if there is an exception
   * @param <T> the return type
   * @return the computed value
   */
  public static <T> T safe(Callable<T> callable, String message) {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(message, e);
    }
  }
}
