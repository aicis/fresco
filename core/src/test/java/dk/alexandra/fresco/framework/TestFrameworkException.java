package dk.alexandra.fresco.framework;

/**
 * An exception thrown when exceptions happen in test framework, e.g. during
 * setUp or tearDown of test fixtures, etc.
 */
public class TestFrameworkException extends RuntimeException {

  private static final long serialVersionUID = -7610884868224471086L;

  public TestFrameworkException(String msg) {
    super(msg);
  }

  public TestFrameworkException(String msg, Throwable e) {
    super(msg, e);
  }

}
