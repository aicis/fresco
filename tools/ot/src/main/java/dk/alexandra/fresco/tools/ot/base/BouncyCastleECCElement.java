package dk.alexandra.fresco.tools.ot.base;

import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

/**
 * Needs the Bouncy Castle dependency from the OT pom.xml
 */
public class BouncyCastleECCElement implements InterfaceNaorPinkasElement<BouncyCastleECCElement> {

  private final ECPoint point;

  public BouncyCastleECCElement(ECPoint point) {
    this.point = point;
  }

  @Override
  public byte[] toByteArray() {
    return point.getEncoded(false);
  }

  @Override
  public BouncyCastleECCElement groupOp(BouncyCastleECCElement other) {
    return new BouncyCastleECCElement(this.point.add(other.point));
  }

  @Override
  public BouncyCastleECCElement inverse() {
    return new BouncyCastleECCElement(this.point.negate());
  }

  @Override
  public BouncyCastleECCElement exponentiation(BigInteger n) {
    return new BouncyCastleECCElement(this.point.multiply(n));
  }
}
