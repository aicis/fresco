package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes the Hamming distance between an array of shared bits and a public value.
 *
 */
public class HammingDistance implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> bits;
  private final BigInteger publicValue;

  public HammingDistance(
      List<DRes<SInt>> bits, BigInteger publicValue) {
    this.bits = bits;
    this.publicValue = publicValue;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    BigInteger one = BigInteger.ONE;
    int length = bits.size();
    BigInteger m = publicValue;
    if (length == 1) {
      if (m.testBit(0)) {
        return builder.numeric().sub(one, bits.get(0));
      } else {
        return bits.get(0);
      }
    } else {
      return builder.par((par) -> {
        List<DRes<SInt>> xor = new ArrayList<>();
        // for each bit i of m negate r_i if m_i is set
        for (int i = 0; i < length; i++) {
          if (m.testBit(i)) {
            xor.add(par.numeric().sub(one, bits.get(i)));
          } else {
            xor.add(bits.get(i));
          }
        }
        return () -> xor;
      }).seq((seq, list) -> seq.advancedNumeric().sum(list)
      );
    }
  }
}
