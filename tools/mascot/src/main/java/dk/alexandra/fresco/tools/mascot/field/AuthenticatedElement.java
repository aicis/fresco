package dk.alexandra.fresco.tools.mascot.field;

import java.math.BigInteger;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.tools.mascot.arithm.Addable;

public class AuthenticatedElement implements Addable<AuthenticatedElement> {

  SpdzElement spdzElement;
  BigInteger modulus;
  int modBitLength;

  public AuthenticatedElement(FieldElement share, FieldElement mac, BigInteger modulus,
      int modBitLength) {
    this.spdzElement = new SpdzElement(share.toBigInteger(), mac.toBigInteger(), modulus);
    this.modulus = modulus;
    this.modBitLength = modBitLength;
  }

  public AuthenticatedElement(SpdzElement spdzElement, BigInteger modulus, int modBitLength) {
    this.spdzElement = spdzElement;
    this.modulus = modulus;
    this.modBitLength = modBitLength;
  }

  @Override
  public AuthenticatedElement add(AuthenticatedElement other) {
    SpdzElement sum = spdzElement.add(other.spdzElement);
    return new AuthenticatedElement(sum, modulus, modBitLength);
  }

  public AuthenticatedElement subtract(AuthenticatedElement other) {
    SpdzElement sum = spdzElement.subtract(other.spdzElement);
    return new AuthenticatedElement(sum, modulus, modBitLength);
  }

  public AuthenticatedElement multiply(FieldElement constant) {
    SpdzElement mult = spdzElement.multiply(constant.toBigInteger());
    return new AuthenticatedElement(mult, modulus, modBitLength);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((spdzElement == null) ? 0 : spdzElement.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AuthenticatedElement other = (AuthenticatedElement) obj;
    if (spdzElement == null) {
      if (other.spdzElement != null)
        return false;
    } else if (!spdzElement.equals(other.spdzElement))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AuthenticatedElement [spdzElement=" + spdzElement + "]";
  }

  public FieldElement getMac() {
    return new FieldElement(spdzElement.getMac(), modulus, modBitLength);
  }

  public FieldElement getShare() {
    return new FieldElement(spdzElement.getShare(), modulus, modBitLength);
  }

}
