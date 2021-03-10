package dk.alexandra.fresco.tools.bitTriples.elements;

import dk.alexandra.fresco.framework.util.StrictBitVector;

public class AuthenticatedElement {

  private final boolean bit;
  private final StrictBitVector mac;

  /**
   * Creates new authenticated element.
   *
   * @param share this party's share
   * @param mac this party's share of the mac
   */
  public AuthenticatedElement(boolean share, StrictBitVector mac) {
    this.bit = share;
    this.mac = mac;
  }

  /**
   * Adds other to this and returns result.
   *
   * @param other value to be added
   * @return sum
   */
  public AuthenticatedElement xor(AuthenticatedElement other) {
    StrictBitVector xorOfMacs =  new StrictBitVector(mac.toByteArray().clone());
    xorOfMacs.xor(other.mac);
    return new AuthenticatedElement(bit ^ other.bit, xorOfMacs);
  }

  /**
   * Adds other to this and returns result.
   *
   * @return sum
   */
  public AuthenticatedElement and(boolean bool) {
    StrictBitVector macShare = new StrictBitVector(mac.getSize());
    if(bool){
      macShare = new StrictBitVector(mac.toByteArray().clone());
    }
    return new AuthenticatedElement(bit && bool, macShare);
  }

  public StrictBitVector getMac() {
    return mac;
  }

  public boolean getBit() {
    return bit;
  }

  @Override
  public String toString() {
    return "AuthenticatedElement ["
        + "share=" + bit
        + ", mac=" + mac
        + ']';
  }
}
