package dk.alexandra.fresco.framework;

public class MaliciousException extends MPCException {

  @Deprecated
  public MaliciousException(Exception cause) {
    super("redundant rethrow - wil be removed", cause);
  }

  public MaliciousException(String message) {
    super(message);
  }

}
