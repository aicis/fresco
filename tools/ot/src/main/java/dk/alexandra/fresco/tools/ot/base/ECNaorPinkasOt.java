package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;

import dk.alexandra.fresco.framework.util.HmacDrbg;
import java.security.Security;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

/**
 * Elliptic curve implementation of the Naor-Pinkas OT
 * Needs the Bouncy Castle dependency from the OT pom.xml
 */
public class ECNaorPinkasOt extends AbstractNaorPinkasOT<ECElement> {

  /**
   * The order of the subgroup we use in the elliptic curve. I.e. the size of the domain of valid
   * points.
   */
  private final BigInteger subgroupOrder;
  /**
   * The generator of the elliptic curve group used in the OT.
   */
  private final org.bouncycastle.math.ec.ECPoint generator;

  private final ECCurve curve;


  public ECNaorPinkasOt(int otherId, Drbg randBit, Network network) {
    super(otherId, randBit, network);
    Security.addProvider(new BouncyCastleProvider());
    X9ECParameters ecP = CustomNamedCurves.getByName("curve25519");
    this.curve = ecP.getCurve();
    this.subgroupOrder = curve.getOrder();
    this.generator = ecP.getG();
  }

  @Override
  ECElement generateRandomNaorPinkasElement() {
    // Sample random integer over the field of the curve
    BigInteger randomInt = this.randNum.nextBigInteger(curve.getField().getCharacteristic());
    // Compute the closets point on the curve to this
    return new ECElement(computePoint(randomInt));
  }

  /**
   * Execute a modified version of try-and-increment, by using rehashing instead of simply
   * incrementing the base point at each iteration.
   * @param xCandidate base candidate for the x coordinate
   * @return A random point on the curve determined from xCandidate
   */
  private ECPoint computePoint(BigInteger xCandidate) {
    ECFieldElement x = this.curve.fromBigInteger(xCandidate);
    ECFieldElement a = this.curve.getA();
    ECFieldElement b = this.curve.getB();
    ECFieldElement ySquare, y;
    ECPoint resPoint;
    do {
      do {
        x = nextFieldElement(x);
        //      = x^3 + ax + b
        ySquare = x.multiply(x.square()).add(a.multiply(x)).add(b);
        y = ySquare.sqrt();
      } while (y == null);
      resPoint = this.curve.createPoint(x.toBigInteger(), y.toBigInteger());
      // Ensure we have a point in the correct subgroup
      resPoint = resPoint.multiply(this.curve.getCofactor()).normalize();
      // Note that we need to validate the point. While multiplying with the co-factor should be
      // sufficient if the result is not the point at infinity
    } while (!resPoint.isValid() && !resPoint.isInfinity());
    return resPoint;
  }

  /**
   * Hash the point to get a new random, but deterministic point on the curve
   * this improves over the classical try-and-increment approach in that it will be random
   * and not biased in case that the spread of valid x points are not uniform
   * @param input the input to derive next integer from
   * @return a new random integer
   */
  ECFieldElement nextFieldElement(ECFieldElement input) {
    HmacDrbg rand = new HmacDrbg(input.getEncoded());
    // The amount of bytes we need is bounded by the field size (in bites),
    // but we take the field size twice before we compute modulo to ensure the distribution is
    // statistically indistinguishable from random with field size being the statistical security
    // parameter
    int bytesNeeded = (2 * curve.getFieldSize()+7) / 8;
    byte[] nextInt = new byte[bytesNeeded];
    rand.nextBytes(nextInt);
    BigInteger rawInt = new BigInteger(1, nextInt);
    return curve.fromBigInteger(rawInt.mod(curve.getField().getCharacteristic()));
  }

  @Override
  ECElement decodeElement(byte[] bytes) {
    // We expect BouncyCastle to verify that the point is correct.
    return new ECElement(this.curve.decodePoint(bytes));
  }

  @Override
  BigInteger getSubgroupOrder() {
    return this.subgroupOrder;
  }

  @Override
  ECElement getGenerator() {
    return new ECElement(this.generator);
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
