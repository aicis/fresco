package dk.alexandra.fresco.tools.commitment;

/**
 * An exception indicating that something, *not* malicious, went wrong while
 * trying to do a commitment.
 * 
 * @author jot2re
 *
 */
public class FailedCommitmentException extends Exception {
  private static final long serialVersionUID = -955008682782924985L;

  public FailedCommitmentException() {
    super();
  }

  public FailedCommitmentException(String message) {
    super(message);
  }
}
