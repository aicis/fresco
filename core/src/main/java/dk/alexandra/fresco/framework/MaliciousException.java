
package dk.alexandra.fresco.framework;

/**
 * Runtime exception to be thrown if and only if data received from another party does not follow
 * the protocol specification. By definition this is considered a malicious act. Data received is
 * not limited to raw data, but also any kind of local processing of raw data.
 */
public class MaliciousException extends RuntimeException {

  private static final long serialVersionUID = -3829588807931116397L;

  public MaliciousException(String message) {
    super(message);
  }

}
