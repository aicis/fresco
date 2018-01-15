
package dk.alexandra.fresco.framework;

/**
 * Runtime exception to be thrown if and only if the code verifies that another party has acted
 * maliciously. Thus this should only be thrown when it is indisputable that a party has acted
 * maliciously.
 */
public class MaliciousException extends MPCException {

  private static final long serialVersionUID = -3829588807931116397L;

  public MaliciousException(String message) {
    super(message);
  }

}
