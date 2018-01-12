package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.tools.mascot.arithm.Addable;

import java.math.BigInteger;

public class AuthenticatedElement implements Addable<AuthenticatedElement> {

  private final FieldElement share;
  private final FieldElement mac;
  private final BigInteger modulus;
  private final int modBitLength;

  /**
   * Creates new authenticated element.
   *
   * @param share this party's share
   * @param mac this party's share of the mac
   * @param modulus modulus of the underlying field elements
   * @param modBitLength bit length of modulus
   */
  public AuthenticatedElement(FieldElement share, FieldElement mac, BigInteger modulus,
      int modBitLength) {
    this.share = share;
    this.mac = mac;
    this.modulus = modulus;
    this.modBitLength = modBitLength;
  }

  @Override
  public AuthenticatedElement add(AuthenticatedElement other) {
    return new AuthenticatedElement(share.add(other.share), mac.add(other.mac), modulus,
        modBitLength);
  }

  public AuthenticatedElement add(FieldElement other, int partyId, FieldElement macKeyShare) {
    FieldElement otherMac = other.multiply(macKeyShare);
    // only party 1 actually adds value to its share
    FieldElement value = (partyId == 1) ? other :
        new FieldElement(BigInteger.ZERO, modulus, modBitLength);
    AuthenticatedElement wrapped = new AuthenticatedElement(value, otherMac, modulus, modBitLength);
    return add(wrapped);
  }

  public AuthenticatedElement subtract(AuthenticatedElement other) {
    return new AuthenticatedElement(share.subtract(other.share), mac.subtract(other.mac), modulus,
        modBitLength);
  }

  public AuthenticatedElement multiply(FieldElement constant) {
    return new AuthenticatedElement(share.multiply(constant), mac.multiply(constant), modulus,
        modBitLength);
  }

  public FieldElement getMac() {
    return mac;
  }

  public FieldElement getShare() {
    return share;
  }

  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public String toString() {
    return "AuthenticatedElement [share=" + share + ", mac=" + mac + ", modulus=" + modulus
        + ", modBitLength=" + modBitLength + "]";
  }

}
