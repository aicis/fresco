package dk.alexandra.fresco.tools.ot.otextension;

/**
 * Exception which indicates that a party has acted maliciously during the
 * execution of the OT extension.
 * 
 * @author jot2re
 *
 */
public class MaliciousOtExtensionException extends Exception {
  private static final long serialVersionUID = -6172975139878874765L;

  public MaliciousOtExtensionException() {
    super();
  }

  public MaliciousOtExtensionException(String message) {
    super(message);
  }
}
