package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;

public class AuthenticatedElement implements Addable<AuthenticatedElement> {

  private final FieldElement share;
  private final FieldElement mac;

  /**
   * Creates new authenticated element.
   *
   * @param share this party's share
   * @param mac this party's share of the mac
   */
  public AuthenticatedElement(FieldElement share, FieldElement mac) {
    this.share = share;
    this.mac = mac;
  }

  /**
   * Adds other to this and returns result.
   *
   * @param other value to be added
   * @return sum
   */
  @Override
  public AuthenticatedElement add(AuthenticatedElement other) {
    return new AuthenticatedElement(share.add(other.share), mac.add(other.mac));
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
      FieldDefinition fieldDefinition,
      FieldElement other, int partyId, FieldElement macKeyShare) {
    FieldElement otherMac = other.multiply(macKeyShare);
    // only party 1 actually adds value to its share
    FieldElement value = (partyId == 1) ? other : fieldDefinition.createElement(0);
    AuthenticatedElement wrapped = new AuthenticatedElement(value, otherMac);
    return add(wrapped);
  }

  /**
   * Subtracts other from this and returns result.
   *
   * @param other value to be subtracted
   * @return difference
   */
  public AuthenticatedElement subtract(AuthenticatedElement other) {
    return new AuthenticatedElement(share.subtract(other.share), mac.subtract(other.mac));
  }

  /**
   * Multiplies this by public, constant value and returns result.
   *
   * @param constant factor
   * @return product
   */
  public AuthenticatedElement multiply(FieldElement constant) {
    return new AuthenticatedElement(share.multiply(constant), mac.multiply(constant));
  }

  public FieldElement getMac() {
    return mac;
  }

  public FieldElement getShare() {
    return share;
  }

  @Override
  public String toString() {
    return "AuthenticatedElement ["
        + "share=" + share
        + ", mac=" + mac
        + ']';
  }
}
