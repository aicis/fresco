package dk.alexandra.fresco.tools.commitment;

/**
 * An exception indicating that a party *is* acting maliciously during the
 * opening of a commitment.
 * 
 * @author jot2re
 *
 */
public class MaliciousCommitmentException extends Exception {
  private static final long serialVersionUID = 4784684598629727562L;

  public MaliciousCommitmentException() {
    super();
  }

  public MaliciousCommitmentException(String message) {
    super(message);
  }
}
