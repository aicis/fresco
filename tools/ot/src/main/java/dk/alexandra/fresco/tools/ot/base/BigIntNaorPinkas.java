package dk.alexandra.fresco.tools.ot.base;import dk.alexandra.fresco.framework.network.Network;import dk.alexandra.fresco.framework.util.Drbg;import javax.crypto.spec.DHParameterSpec;import java.math.BigInteger;public class BigIntNaorPinkas extends AbstractNaorPinkasOT<BigIntElement> {  /**   * The modulus of the Diffie-Hellman group used in the OT.   */  private final BigInteger dhModulus;  /**   * The generator of the Diffie-Hellman group used in the OT.   */  private final BigInteger dhGenerator;  public BigIntNaorPinkas(int otherId, Drbg randBit, Network network) {    super(otherId, randBit, network);    DHParameterSpec params = DhParameters.getStaticDhParams();    this.dhModulus = params.getP();    this.dhGenerator = params.getG();  }  @Override  BigIntElement generateRandomNaorPinkasElement() {    return new BigIntElement(randNum.nextBigInteger(dhModulus), this.dhModulus);  }  @Override  BigIntElement decodeElement(byte[] bytes) {    return new BigIntElement(new BigInteger(1, bytes), this.dhModulus);  }  @Override  BigInteger getSubgroupOrder() {    return this.dhModulus;  }  @Override  BigIntElement getGenerator() {    return new BigIntElement(this.dhGenerator, this.dhModulus);  }}