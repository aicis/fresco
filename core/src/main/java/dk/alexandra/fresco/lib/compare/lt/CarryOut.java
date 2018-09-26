package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given values a and b represented as bits, computes if a + b overflows, i.e., if the addition
 * results in a carry.
 */
public class CarryOut implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<List<OInt>> openBitsDef;
  private final DRes<List<DRes<SInt>>> secretBitsDef;
  private final DRes<OInt> carryIn;
  private final boolean reverseSecretBits;

  /**
   * Constructs new {@link CarryOut}.
   *
   * @param clearBits clear bits
   * @param secretBits secret bits
   * @param carryIn an additional carry-in bit which we add to the least-significant bits of the
   * inputs
   * @param reverseSecretBits indicates whether secret bits need to be reverse (to account for
   * endianness)
   */
  public CarryOut(DRes<List<OInt>> clearBits, DRes<List<DRes<SInt>>> secretBits,
      DRes<OInt> carryIn, boolean reverseSecretBits) {
    this.secretBitsDef = secretBits;
    this.openBitsDef = clearBits;
    this.carryIn = carryIn;
    this.reverseSecretBits = reverseSecretBits;
  }

  public CarryOut(DRes<List<OInt>> clearBits, DRes<List<DRes<SInt>>> secretBits,
      DRes<OInt> carryIn) {
    this(clearBits, secretBits, carryIn, false);
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> secretBits = secretBitsDef.out();
    if (reverseSecretBits) {
      Collections.reverse(secretBits);
    }
    List<OInt> openBits = openBitsDef.out();
    if (secretBits.size() != openBits.size()) {
      throw new IllegalArgumentException("Number of bits must be the same");
    }
    return builder.par(par -> {
      DRes<List<DRes<SInt>>> xored = par.logical().pairWiseXorKnown(openBitsDef, secretBitsDef);
      DRes<List<DRes<SInt>>> anded = par.logical().pairWiseAndKnown(openBitsDef, secretBitsDef);
      final Pair<DRes<List<DRes<SInt>>>, DRes<List<DRes<SInt>>>> pair = new Pair<>(xored,
          anded);
      return () -> pair;
    }).par((par, pair) -> {
      List<DRes<SInt>> xoredBits = pair.getFirst().out();
      List<DRes<SInt>> andedBits = pair.getSecond().out();
      List<SIntPair> pairs = new ArrayList<>(andedBits.size());
      for (int i = 0; i < secretBits.size(); i++) {
        DRes<SInt> xoredBit = xoredBits.get(i);
        DRes<SInt> andedBit = andedBits.get(i);
        pairs.add(new SIntPair(xoredBit, andedBit));
      }
      return () -> pairs;
    }).seq((seq, pairs) -> {
      // need to account for carry-in bit
      int lastIdx = pairs.size() - 1;
      SIntPair lastPair = pairs.get(lastIdx);
      DRes<SInt> lastCarryPropagator = seq.logical().halfOr(
          lastPair.getSecond(),
          seq.logical().andKnown(carryIn, lastPair.getFirst()));
      pairs.set(lastIdx, new SIntPair(lastPair.getFirst(), lastCarryPropagator));
      return seq.seq(new PreCarryBits(pairs));
    });
  }

}
