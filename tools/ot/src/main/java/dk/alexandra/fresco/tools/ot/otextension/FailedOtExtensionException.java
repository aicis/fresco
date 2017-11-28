package dk.alexandra.fresco.tools.ot.otextension;

public class FailedOtExtensionException extends Exception {
  private static final long serialVersionUID = 7470027014489625203L;
  
  public FailedOtExtensionException() {
    super();
  }

  public FailedOtExtensionException(String message) {
    super(message);
  }
}
