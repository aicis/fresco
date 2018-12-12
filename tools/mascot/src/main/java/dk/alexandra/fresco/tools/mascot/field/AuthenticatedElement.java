package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import java.math.BigInteger;

public class AuthenticatedElement implements Addable<AuthenticatedElement> {

  private final MascotFieldElement share;
  private final MascotFieldElement mac;
  private final BigInteger modulus;

  /**
   * Creates new authenticated element.
   *
   * @param share this party's share
   * @param mac this party's share of the mac
   * @param modulus modulus of the underlying field elements
   */
  public AuthenticatedElement(MascotFieldElement share, MascotFieldElement mac,
      BigInteger modulus) {
    this.share = share;
    this.mac = mac;
    this.modulus = modulus;
  }

  /**
   * Adds other to this and returns result.
   * @param other value to be added
   * @return sum
   */
  @Override
  public AuthenticatedElement add(AuthenticatedElement other) {
    return new AuthenticatedElement(share.add(other.share), mac.add(other.mac), modulus);
  }

  /**
   * Adds constant (open) value to this and returns result. <p>All parties compute their mac share
   * of the public value and add it to the mac share of the authenticated value, however only party
   * 1 adds the public value to is value share.</p>
   *
   * @param other constant, open value
   * @param partyId party ID used to ensure that only one party adds value to share
   * @param macKeyShare mac key share for maccing open value
   * @return result of sum
   */
  public AuthenticatedElement add(
      MascotFieldElement other, int partyId, MascotFieldElement macKeyShare) {
    MascotFieldElement otherMac = other.multiply(macKeyShare);
    // only party 1 actually adds value to its share
    MascotFieldElement value = (partyId == 1) ? other :
        new MascotFieldElement(BigInteger.ZERO, modulus);
    AuthenticatedElement wrapped = new AuthenticatedElement(value, otherMac, modulus);
    return add(wrapped);
  }

  /**
   * Subtracts other from this and returns result.
   * @param other value to be subtracted
   * @return difference
   */
  public AuthenticatedElement subtract(AuthenticatedElement other) {
    return new AuthenticatedElement(share.subtract(other.share), mac.subtract(other.mac), modulus);
  }

  /**
   * Multiplies this by public, constant value and returns result.
   * @param constant factor
   * @return product
   */
  public AuthenticatedElement multiply(MascotFieldElement constant) {
    return new AuthenticatedElement(share.multiply(constant), mac.multiply(constant), modulus);
  }

  public MascotFieldElement getMac() {
    return mac;
  }

  public MascotFieldElement getShare() {
    return share;
  }

  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public String toString() {
    return "AuthenticatedElement [" +
        "share=" + share +
        ", mac=" + mac +
        ']';
  }
}
