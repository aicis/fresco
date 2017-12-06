package dk.alexandra.fresco.tools.mascot.maccheck;

public class FailedMacCheckException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -773950035194624351L;

  public FailedMacCheckException() {
    super();
  }
  
  public FailedMacCheckException(String message) {
    super(message);
  }

  public FailedMacCheckException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
