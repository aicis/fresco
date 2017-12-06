package dk.alexandra.fresco.tools.mascot.maccheck;

public class MaliciousMacCheckException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -773950035194624351L;

  public MaliciousMacCheckException() {
    super();
  }
  
  public MaliciousMacCheckException(String message) {
    super(message);
  }

  public MaliciousMacCheckException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
