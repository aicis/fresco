package dk.alexandra.fresco.tools.ot.base;

import iaik.security.ec.math.curve.ECPoint;

import java.math.BigInteger;

public class ECCelerateElement implements InterfaceNaorPinkasElement {

  ECPoint point;

  public ECCelerateElement(ECPoint point) {
    this.point = point;
  }

  @Override
  public byte[] toByteArray() {
    return this.point.encodePoint();
  }

  @Override
  public InterfaceNaorPinkasElement groupOp(InterfaceNaorPinkasElement other) {
    ECCelerateElement otherEcc = (ECCelerateElement) other;
    ECPoint tmpPoint = this.point.clone();
    return new ECCelerateElement(tmpPoint.addPoint(otherEcc.point));
  }

  @Override
  public InterfaceNaorPinkasElement inverse() {
    return new ECCelerateElement(this.point.clone().negatePoint());
  }

  @Override
  public InterfaceNaorPinkasElement exponentiation(BigInteger n) {
    ECPoint tmpPoint = this.point.clone();
    return new ECCelerateElement(tmpPoint.multiplyPoint(n));
  }

}
