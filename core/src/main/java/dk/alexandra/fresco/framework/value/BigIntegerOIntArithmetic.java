package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.DRes;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link OIntArithmetic} for the case when open values are represented directly
 * via {@link BigInteger}.
 */
public class BigIntegerOIntArithmetic implements OIntArithmetic {

  private final OIntFactory factory;

  public BigIntegerOIntArithmetic(OIntFactory factory) {
    this.factory = factory;
  }

  @Override
  public List<DRes<OInt>> toBits(OInt openValue, int numBits) {
    BigInteger value = factory.toBigInteger(openValue);
    List<DRes<OInt>> bits = new ArrayList<>(numBits);
    for (int b = 0; b < numBits; b++) {
      boolean boolBit = value.testBit(b);
      OInt bit = factory.fromLong(boolBit ? 1 : 0);
      bits.add(() -> bit);
    }
    Collections.reverse(bits);
    return bits;
  }

}
