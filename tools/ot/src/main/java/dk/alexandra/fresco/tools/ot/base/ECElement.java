package dk.alexandra.fresco.tools.ot.base;

import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

/**
 * Needs the Bouncy Castle dependency from the OT pom.xml
 */
public class ECElement implements InterfaceOtElement<ECElement> {

    private final ECPoint point;

    public ECElement(ECPoint point) {
        this.point = point;
    }

    @Override
    public byte[] toByteArray() {
        return point.getEncoded(false);
    }

    @Override
    public ECElement groupOp(ECElement other) {
        return new ECElement(this.point.add(other.point));
    }

    @Override
    public ECElement inverse() {
        return new ECElement(this.point.negate());
    }

    @Override
    public ECElement exponentiation(BigInteger n) {
        return new ECElement(this.point.multiply(n));
    }
}