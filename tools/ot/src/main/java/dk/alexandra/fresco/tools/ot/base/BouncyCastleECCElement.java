package dk.alexandra.fresco.tools.ot.base;

import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

/**
 * Needs the Bouncy Castle dependency from the OT pom.xml
 */
public class BouncyCastleECCElement implements InterfaceNaorPinkasElement {

  ECPoint point;

  public BouncyCastleECCElement(ECPoint point) {
    this.point = point;
  }

  @Override
  public byte[] toByteArray() {
    return point.getEncoded(false);
  }

  @Override
  public InterfaceNaorPinkasElement groupOp(InterfaceNaorPinkasElement other) {
    BouncyCastleECCElement otherBC = (BouncyCastleECCElement) other;
    return new BouncyCastleECCElement(this.point.add(otherBC.point));
  }

  @Override
  public InterfaceNaorPinkasElement inverse() {
    return new BouncyCastleECCElement(this.point.negate());
  }

  @Override
  public InterfaceNaorPinkasElement exponentiation(BigInteger n) {
    return new BouncyCastleECCElement(this.point.multiply(n));
  }
}
