package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes the Hamming distance between an array of shared bits and a public value
 *
 * @author ttoft
 */
public class HammingDistance implements ComputationBuilder<SInt> {

  private final List<Computation<SInt>> aBits;
  private final OInt b;

  public HammingDistance(
      List<Computation<SInt>> aBits, OInt b) {
    this.aBits = aBits;
    this.b = b;
  }

  @Override
  public Computation<SInt> build(SequentialProtocolBuilder builder) {
    OInt one = builder.getOIntFactory().getOInt(BigInteger.ONE);
    int length = aBits.size();
    BigInteger m = b.getValue();
    if (length == 1) {
      if (m.testBit(0)) {
        return builder.numeric().sub(one, aBits.get(0));
      } else {
        return aBits.get(0);
      }
    } else {
      return builder.par((par) -> {
        List<Computation<SInt>> xor = new ArrayList<>();
        // for each bit i of m negate r_i if m_i is set
        for (int i = 0; i < length; i++) {
          if (m.testBit(i)) {
            xor.add(par.numeric().sub(one, aBits.get(i)));
          } else {
            xor.add(aBits.get(i));
          }
        }
        return () -> xor;
      }).seq((list, seq) ->
          new SumSIntList(list).build(seq)
      );
    }
  }
}
