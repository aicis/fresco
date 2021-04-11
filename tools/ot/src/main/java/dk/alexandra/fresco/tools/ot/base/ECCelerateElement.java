package dk.alexandra.fresco.tools.ot.base;

import iaik.security.ec.math.curve.ECPoint;

import java.math.BigInteger;

public class ECCelerateElement implements InterfaceNaorPinkasElement<ECCelerateElement> {

  private final ECPoint point;

  public ECCelerateElement(ECPoint point) {
    this.point = point;
  }

  @Override
  public byte[] toByteArray() {
    return this.point.encodePoint();
  }

  @Override
  public ECCelerateElement groupOp(ECCelerateElement other) {
    ECPoint tmpPoint = this.point.clone();
    return new ECCelerateElement(tmpPoint.addPoint(other.point));
  }

  @Override
  public ECCelerateElement inverse() {
    return new ECCelerateElement(this.point.clone().negatePoint());
  }

  @Override
  public ECCelerateElement exponentiation(BigInteger n) {
    ECPoint tmpPoint = this.point.clone();
    return new ECCelerateElement(tmpPoint.multiplyPoint(n));
  }

}
