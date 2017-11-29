package dk.alexandra.fresco.tools.ot.otextension;

/**
 * Exception indicating that something, non-malicious, went wrong during the
 * execution of OT extension.
 * 
 * @author jot2re
 *
 */
public class FailedOtExtensionException extends Exception {
  private static final long serialVersionUID = 7470027014489625203L;
  
  public FailedOtExtensionException() {
    super();
  }

  public FailedOtExtensionException(String message) {
    super(message);
  }
}
