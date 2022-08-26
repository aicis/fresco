package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;

import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;

public class BigIntNaorPinkas extends AbstractNaorPinkasOT {

  /**
   * The modulus of the Diffie-Hellman group used in the OT.
   */
  private final BigInteger dhModulus;
  /**
   * The generator of the Diffie-Hellman group used in the OT.
   */
  private final BigInteger dhGenerator;

  public BigIntNaorPinkas(int otherId, Drbg randBit, Network network) {
    super(otherId, randBit, network);
    DHParameterSpec params = DhParameters.getStaticDhParams();
    this.dhModulus = params.getP();
    this.dhGenerator = params.getG();
  }

  @Override
  InterfaceNaorPinkasElement generateRandomNaorPinkasElement() {
    return new BigIntElement(randNum.nextBigInteger(dhModulus), this.dhModulus);
  }

  @Override
  InterfaceNaorPinkasElement decodeElement(byte[] bytes) {
    return new BigIntElement(new BigInteger(bytes), this.dhModulus);
  }

  @Override
  BigInteger getDhModulus() {
    return this.dhModulus;
  }

  @Override
  InterfaceNaorPinkasElement getDhGenerator() {
    return new BigIntElement(this.dhGenerator, this.dhModulus);
  }
}
