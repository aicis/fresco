package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import java.security.Security;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECCurve;

import java.math.BigInteger;

/**
 * Needs the Bouncy Castle dependency from the OT pom.xml
 */
public class BouncyCastleNaorPinkas extends AbstractNaorPinkasOT {

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
  InterfaceNaorPinkasElement generateRandomNaorPinkasElement() {
    return new BouncyCastleECCElement(
        this.dhGenerator.multiply(this.randNum.nextBigInteger(this.dhModulus)));
  }

  @Override
  InterfaceNaorPinkasElement decodeElement(byte[] bytes) {
    return new BouncyCastleECCElement(this.curve.decodePoint(bytes));
  }

  @Override
  BigInteger getDhModulus() {
    return this.dhModulus;
  }

  @Override
  InterfaceNaorPinkasElement getDhGenerator() {
    return new BouncyCastleECCElement(this.dhGenerator);
  }
}
