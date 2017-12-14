package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.tools.mascot.arithm.Addable;

import java.math.BigInteger;

public class AuthenticatedElement implements Addable<AuthenticatedElement> {

  FieldElement share;
  FieldElement mac;
  BigInteger modulus;
  int modBitLength;

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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mac == null) ? 0 : mac.hashCode());
    result = prime * result + modBitLength;
    result = prime * result + ((modulus == null) ? 0 : modulus.hashCode());
    result = prime * result + ((share == null) ? 0 : share.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AuthenticatedElement other = (AuthenticatedElement) obj;
    if (mac == null) {
      if (other.mac != null) {
        return false;
      }
    } else if (!mac.equals(other.mac)) {
      return false;
    }
    if (modBitLength != other.modBitLength) {
      return false;
    }
    if (modulus == null) {
      if (other.modulus != null) {
        return false;
      }
    } else if (!modulus.equals(other.modulus)) {
      return false;
    }
    if (share == null) {
      if (other.share != null) {
        return false;
      }
    } else if (!share.equals(other.share)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "AuthenticatedElement [share=" + share + ", mac=" + mac + ", modulus=" + modulus
        + ", modBitLength=" + modBitLength + "]";
  }

}
