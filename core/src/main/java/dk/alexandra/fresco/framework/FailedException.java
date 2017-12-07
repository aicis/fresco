package dk.alexandra.fresco.framework;

public class FailedException extends MPCException {

  @Deprecated
  public FailedException(Exception cause) {
    super("redundant rethrow - wil be removed", cause);
  }

  public FailedException(String message, Exception cause) {
    super(message, cause);
  }
}
