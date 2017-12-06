package dk.alexandra.fresco.tools.mascot.elgen;

public class FailedElGenException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -773950035194624351L;

  public FailedElGenException() {
    super();
  }
  
  public FailedElGenException(String message) {
    super(message);
  }

  public FailedElGenException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
