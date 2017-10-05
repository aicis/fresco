package dk.alexandra.fresco.framework;

/**
 * Generic exception which can be cast from within FRESCO to indicate that something went wrong.
 * 
 */
public class MPCException extends RuntimeException {

  private static final long serialVersionUID = -7610884868224471086L;

  public MPCException(String msg) {
    super(msg);
  }

  public MPCException(String msg, Exception e) {
    super(msg, e);
  }

}
