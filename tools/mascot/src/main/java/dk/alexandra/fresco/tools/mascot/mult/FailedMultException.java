package dk.alexandra.fresco.tools.mascot.mult;

public class FailedMultException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 3756912425763637515L;

  public FailedMultException() {
    super();
  }

  public FailedMultException(String message) {
    super(message);
  }

  public FailedMultException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedMultException(Throwable cause) {
    super(cause);
  }

}
