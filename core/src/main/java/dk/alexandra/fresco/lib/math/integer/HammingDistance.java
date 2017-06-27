package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.FrescoFunction;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.util.Pair;
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
public class HammingDistance implements FrescoFunction<Pair<List<Computation<SInt>>, OInt>, SInt> {

  @Override
  public Computation<SInt> apply(Pair<List<Computation<SInt>>, OInt> inputPair,
      SequentialProtocolBuilder builder) {
    OInt one = builder.getOIntFactory().getOInt(BigInteger.ONE);
    List<Computation<SInt>> aBits = inputPair.getFirst();
    int length = aBits.size();
    BigInteger m = inputPair.getSecond().getValue();
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
      }).seq(new SumSIntList());
    }
  }
}
