package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given known value a and secret value b represented as bits, computes a <? b.
 */
public class BitLessThan implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<BigInteger> openValueDef;
  private final DRes<List<DRes<SInt>>> secretBitsDef;

  public BitLessThan(DRes<BigInteger> openValue, DRes<List<DRes<SInt>>> secretBits) {
    this.openValueDef = openValue;
    this.secretBitsDef = secretBits;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> secretBits = secretBitsDef.out();
    BigInteger openValueA = openValueDef.out();
    int numBits = secretBits.size();
    List<DRes<BigInteger>> openBits = toBits(openValueA, numBits);
    DRes<List<DRes<SInt>>> secretBitsNegated = builder.par(par -> {
      List<DRes<SInt>> negatedBits = new ArrayList<>(numBits);
      for (DRes<SInt> secretBit : secretBits) {
        negatedBits.add(par.numeric().sub(BigInteger.ONE, secretBit));
      }
      Collections.reverse(negatedBits);
      return () -> negatedBits;
    });
    DRes<SInt> gt = builder.seq(new CarryOut(secretBitsNegated, () -> openBits));
    return builder.numeric().sub(BigInteger.ONE, gt);
  }

  /**
   * Turns input value into bits in big-endian order.
   */
  private List<DRes<BigInteger>> toBits(BigInteger value, int numBits) {
    List<DRes<BigInteger>> bits = new ArrayList<>(numBits);
    for (int b = 0; b < numBits; b++) {
      boolean boolBit = value.testBit(b);
      bits.add(boolBit ? () -> BigInteger.ONE : () -> BigInteger.ZERO);
    }
    Collections.reverse(bits);
    return bits;
  }

}
