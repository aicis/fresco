package dk.alexandra.fresco.tools.cointossing;

/**
 * An exception indicating that something, *not* malicious, went wrong while
 * trying to do coin tossing.
 * 
 * @author jot2re
 *
 */
public class FailedCoinTossingException extends Exception {
  private static final long serialVersionUID = -8310097846577362044L;

  public FailedCoinTossingException() {
    super();
  }

  public FailedCoinTossingException(String message) {
    super(message);
  }
}
