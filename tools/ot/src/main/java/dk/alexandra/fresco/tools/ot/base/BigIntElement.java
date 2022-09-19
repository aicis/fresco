package dk.alexandra.fresco.tools.ot.base;

import org.bouncycastle.crypto.digests.SHAKEDigest;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.bouncycastle.pqc.math.linearalgebra.BigEndianConversions.I2OSP;

public class BigIntElement implements InterfaceOtElement<BigIntElement> {

  private final BigInteger element;
  private final BigInteger dhModulus;

  private final int bitsize;

  public BigIntElement(BigInteger element, BigInteger dhModulus) {
    this.element = element;
    this.dhModulus = dhModulus;
    this.bitsize = dhModulus.bitLength();
  }

  @Override
  public byte[] toByteArray() {
    // Ensure the bit length always is the same.
    byte[] val = this.element.mod(this.dhModulus).toByteArray();
    byte[] out = new byte[bitsize/8];
    int startPos = val.length - bitsize/8;
    if (val[0] == 0x00) {
      // drop the first byte.
      System.arraycopy(val, 1, out, startPos - 1, val.length - 1);
    } else {
      System.arraycopy(val, 0, out, startPos, val.length);
    }
    return out;
  }

  @Override
  public BigIntElement groupOp(BigIntElement other) {
    return new BigIntElement(this.element.multiply(other.element), this.dhModulus);
  }

  @Override
  public BigIntElement inverse() {
    return new BigIntElement(this.element.modInverse(this.dhModulus), this.dhModulus);
  }

  @Override
  //modPow in this case
  public BigIntElement exponentiation(BigInteger n) {
    return new BigIntElement(this.element.modPow(n, this.dhModulus), this.dhModulus);
  }


}
