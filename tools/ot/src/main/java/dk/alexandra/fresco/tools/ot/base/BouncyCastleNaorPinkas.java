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
    return new BouncyCastleECCElement(computePoint(this.randNum.nextBigInteger(this.dhModulus)));
  }

  // Specific for curve25519 in Montgomery form
  private ECPoint computePoint(BigInteger integer) {
    ECFieldElement x = this.curve.fromBigInteger(integer);
    ECFieldElement a = this.curve.getA();
    ECFieldElement ySquare, y;
    ECPoint resPoint;
    do {
      x = x.addOne();
      //      = x^3 + ax^2 + x
      ySquare = x.multiply(x.square()).add(a.multiply(x.square())).add(x);

//      BigInteger x1 = x.toBigInteger();
//      ySquare = this.curve.fromBigInteger(
//              x1.modPow(BigInteger.valueOf(3), this.dhModulus)
//                      .add(a.toBigInteger().multiply(x1.modPow(BigInteger.valueOf(2), this.dhModulus)))
//                      .add(x1).mod(this.dhModulus)
//      );

      y = ySquare.sqrt();
    } while (y == null);
    resPoint = this.curve.createPoint(x.toBigInteger(), y.toBigInteger()).normalize();

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

  private static BigInteger calculateResidue(BigInteger p) {
    int bitLength = p.bitLength();
    if (bitLength >= 96) {
      BigInteger firstWord = p.shiftRight(bitLength - 64);
      if (firstWord.longValue() == -1L) {
        return BigInteger.ONE.shiftLeft(bitLength).subtract(p);
      }
    }
    return BigInteger.ZERO;
  }
}
