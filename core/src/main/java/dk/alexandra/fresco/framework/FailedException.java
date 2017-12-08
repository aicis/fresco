package dk.alexandra.fresco.framework;

// TODO Either wriote a meaningful javadoc, or replace the usages with a simple runtime exception
public class FailedException extends MPCException {

  @Deprecated
  public FailedException(Exception cause) {
    super("redundant rethrow - wil be removed", cause);
  }

  public FailedException(String message, Exception cause) {
    super(message, cause);
  }
}
