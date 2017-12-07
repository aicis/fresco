package dk.alexandra.fresco.framework;

public class FailedException extends MPCException {

  public FailedException(String message, Exception cause) {
    super(message, cause);
  }
}
