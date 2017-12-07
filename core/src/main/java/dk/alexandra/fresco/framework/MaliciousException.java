package dk.alexandra.fresco.framework;

public class MaliciousException extends MPCException {

  public MaliciousException(String message, Exception cause) {
    super(message, cause);
  }

  public MaliciousException(String message) {
    super(message);
  }

}
