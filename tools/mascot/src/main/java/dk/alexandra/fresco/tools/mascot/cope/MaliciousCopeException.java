package dk.alexandra.fresco.tools.mascot.cope;

public class MaliciousCopeException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -773950035194624351L;

  public MaliciousCopeException() {
    super();
  }
  
  public MaliciousCopeException(String message) {
    super(message);
  }

  public MaliciousCopeException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
