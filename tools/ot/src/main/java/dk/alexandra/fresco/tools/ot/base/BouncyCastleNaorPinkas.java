package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;

import java.security.SecureRandom;
import java.security.Security;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.spec.ECField;

/**
 * Needs the Bouncy Castle dependency from the OT pom.xml
 */
public class BouncyCastleNaorPinkas extends AbstractNaorPinkasOT<BouncyCastleECCElement> {

  /**
   * The modulus of the Diffie-Hellman group used in the OT.
   */
  private final BigInteger dhModulus;
  /**
   * The generator of the Diffie-Hellman group used in the OT.
   */
  private final org.bouncycastle.math.ec.ECPoint dhGenerator;

  private final ECCurve curve;


  public BouncyCastleNaorPinkas(int otherId, Drbg randBit, Network network) {
    super(otherId, randBit, network);
    Security.addProvider(new BouncyCastleProvider());
    X9ECParameters ecP = CustomNamedCurves.getByName("curve25519");
    this.curve = ecP.getCurve();
    this.dhModulus = curve.getOrder();
    this.dhGenerator = ecP.getG();
  }

  @Override
  BouncyCastleECCElement generateRandomNaorPinkasElement() {
    return new BouncyCastleECCElement(computePoint(this.randNum.nextBigInteger(curve.getField().getCharacteristic())));
  }

  // Specific for curve25519 in Montgomery form
  private ECPoint computePoint(BigInteger integer) {
    ECFieldElement x = this.curve.fromBigInteger(integer);
    ECFieldElement a = this.curve.getA();
    ECFieldElement b = this.curve.getB();
    ECFieldElement ySquare, y;
    ECPoint resPoint;
    do {
      do {
        x = x.addOne();
        //      = x^3 + ax + b
        ySquare = x.multiply(x.square()).add(a.multiply(x)).add(b);
        y = ySquare.sqrt();
      } while (y == null);
      resPoint = this.curve.createPoint(x.toBigInteger(), y.toBigInteger());
      // Ensure we have a point in the correct subgroup
      resPoint = resPoint.multiply(this.curve.getCofactor()).normalize();
    } while (!resPoint.isValid());
    if (!resPoint.isValid()) {
      throw new IllegalStateException("EC Point is not a valid curve25519 point");
    }
    return resPoint;
  }

  @Override
  BouncyCastleECCElement decodeElement(byte[] bytes) {
    return new BouncyCastleECCElement(this.curve.decodePoint(bytes));
  }

  @Override
  BigInteger getDhModulus() {
    return this.dhModulus;
  }

  @Override
  BouncyCastleECCElement getDhGenerator() {
    return new BouncyCastleECCElement(this.dhGenerator);
  }

}
