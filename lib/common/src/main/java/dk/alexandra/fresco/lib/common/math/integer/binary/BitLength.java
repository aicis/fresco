package dk.alexandra.fresco.lib.common.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.util.List;

/**
 * Finds the bit length of an integer. This is done by finding the bit representation of the integer
 * and then returning the index of the highest set bit.
 */
public class BitLength implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int maxBitLength;

  /**
   * Create a protocol for finding the bit length of an integer. This is done by finding the bit
   * representation of the integer and then returning the index of the highest set bit.
   *
   * @param input        An integer.
   * @param maxBitLength An upper bound for the bit length.
   */
  public BitLength(DRes<SInt> input, int maxBitLength) {
    this.input = input;
    this.maxBitLength = maxBitLength;

  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> AdvancedNumeric.using(seq).toBits(input, maxBitLength))
        .seq((seq, bits) ->
                new HighestBit(bits).buildComputation(seq)
            // HighestBit returns the highest set bit counting from zero, so we add 1 to get the bit length
        ).seq((seq, hb) -> seq.numeric().mult(hb.getFirst(), seq.numeric().add(1, hb.getSecond())));
  }

  /**
   * Given a list of bits, this computation returns a pair secret integer. The first is a bit
   * indicating whether there are any set bits in the given list. The second is the index of the
   * highest set bit. The second is zero if no bit has been set.
   */
  private static class HighestBit implements
      Computation<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

    private final List<DRes<SInt>> bits;

    /**
     * Create a new HighestBit computation.
     *
     * @param bits A list of bits.
     */
    public HighestBit(List<DRes<SInt>> bits) {
      this.bits = bits;
    }

    @Override
    public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
      // Compute recursively until the length is 1. In this case it has a highest set bit if
      if (bits.size() == 1) {
        return Pair.lazy(bits.get(0), builder.numeric().known(0));
      }

      return builder.par(par -> {
        int n = bits.size();
        return Pair.lazy(new HighestBit(bits.subList(0, n / 2)).buildComputation(par),
            new HighestBit(bits.subList(n / 2, n)).buildComputation(par));
      }).pairInPar((seq, hb) -> {
        DRes<SInt> upperHasSetBit = hb.getSecond().out().getFirst();
        DRes<SInt> lowerHasSetBit = hb.getFirst().out().getFirst();
        Numeric numeric = seq.numeric();
        DRes<SInt> hasSetBit = numeric
            .sub(numeric.add(upperHasSetBit, lowerHasSetBit),
                numeric.mult(upperHasSetBit, lowerHasSetBit));
        return hasSetBit;
      }, (seq, hb) -> {
        DRes<SInt> upperHasSetBit = hb.getSecond().out().getFirst();
        DRes<SInt> upperHighestBit = hb.getSecond().out().getSecond();
        DRes<SInt> lowerHighestBit = hb.getFirst().out().getSecond();
        Numeric numeric = seq.numeric();
        DRes<SInt> highestBit = numeric.add(
            numeric.mult(upperHasSetBit,
                numeric.add(bits.size() / 2, upperHighestBit)),
            numeric.mult(numeric.sub(1, upperHasSetBit),
                lowerHighestBit));
        return highestBit;
      }).seq((seq, result) -> Pair.lazy(DRes.of(result.getFirst()), DRes.of(result.getSecond())));
    }
  }

}
