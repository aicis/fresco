package dk.alexandra.fresco.tools.mascot.field;

import java.math.BigInteger;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.tools.mascot.ArithmeticElement;

public class AuthenticatedElement implements ArithmeticElement<AuthenticatedElement> {

  SpdzElement spdzElement;
  
  public AuthenticatedElement(FieldElement share, FieldElement mac, BigInteger modulus) {
    this.spdzElement = new SpdzElement(share.toBigInteger(), mac.toBigInteger(), modulus);
  }
  
  public AuthenticatedElement(SpdzElement spdzElement) {
    this.spdzElement = spdzElement;
  }

  @Override
  public AuthenticatedElement add(AuthenticatedElement other) {
    SpdzElement sum = spdzElement.add(other.spdzElement);
    return new AuthenticatedElement(sum);
  }

  @Override
  public AuthenticatedElement multiply(AuthenticatedElement other) {
    throw new UnsupportedOperationException();
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

}
