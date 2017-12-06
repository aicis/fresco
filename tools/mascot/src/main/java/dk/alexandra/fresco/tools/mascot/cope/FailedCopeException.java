package dk.alexandra.fresco.tools.mascot.cope;

public class FailedCopeException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -773950035194624351L;

  public FailedCopeException() {
    super();
  }
  
  public FailedCopeException(String message) {
    super(message);
  }

  public FailedCopeException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
