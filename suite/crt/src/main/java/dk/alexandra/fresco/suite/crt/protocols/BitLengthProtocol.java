package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;
import java.util.List;

/**
 * Compute <i>(h,l)</i> where <i>h</i> is the bit length of <code>input</code> value and <i>l</i> is
 * a power of two such that <i>value * l</i> has bit length equal to <code>maxBitLength</code>.
 */
public class BitLengthProtocol extends CRTComputation<Pair<DRes<SInt>, DRes<SInt>>> {

  private final DRes<SInt> value;
  private final int maxBitLength;

  /**
   * Create a new BitLengthProtocol
   *
   * @param value The input value
   * @param maxBitLength An upper bound for the bit length.
   */
  public BitLengthProtocol(DRes<SInt> value, int maxBitLength) {
    this.value = value;
    this.maxBitLength = maxBitLength;
  }

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder,
      CRTRingDefinition ring,
      CRTNumericContext context) {

    return builder.seq(new BitDecompositionProtocol(value, maxBitLength))
        .seq((seq, bits) -> new HighestSetBitProtocolInternal(bits).buildComputation(seq))
        .seq((seq, bhl) -> Pair
            .lazy(seq.numeric().mult(seq.numeric().add(1, bhl.index), bhl.anyBitSet),
                bhl.normalizer));
  }

  static class HighestSetBitOutput implements DRes<HighestSetBitOutput> {

    final DRes<SInt> anyBitSet;
    final DRes<SInt> index;
    final DRes<SInt> normalizer;

    private HighestSetBitOutput(DRes<SInt> anyBit, DRes<SInt> index, DRes<SInt> normalizer) {
      this.anyBitSet = anyBit;
      this.index = index;
      this.normalizer = normalizer;
    }

    @Override
    public HighestSetBitOutput out() {
      return this;
    }
  }

  /**
   * Compute (b, h, l) where b is 1 if any bit in the list was set and 0 otherwise, h is the index
   * of the highest set bit and l is a power of two which multiplied with the value represented by
   * the bits has bit length bits.size().
   */
  private static class HighestSetBitProtocolInternal extends CRTComputation<HighestSetBitOutput> {

    private final List<DRes<SInt>> bits;

    public HighestSetBitProtocolInternal(List<DRes<SInt>> bits) {
      this.bits = bits;
    }

    @Override
    public DRes<HighestSetBitOutput> buildComputation(ProtocolBuilderNumeric builder,
        CRTRingDefinition ring,
        CRTNumericContext context) {
      if (bits.size() == 1) {
        return builder.par(par -> new HighestSetBitOutput(bits.get(0), par.numeric().known(0),
            par.numeric().sub(2, bits.get(0))));
      }

      int N = bits.size() / 2;

      return builder.par(par -> {
        DRes<HighestSetBitOutput> lower = new HighestSetBitProtocolInternal(bits.subList(0, N))
            .buildComputation(par);
        DRes<HighestSetBitOutput> upper = new HighestSetBitProtocolInternal(
            bits.subList(N, bits.size())).buildComputation(par);
        return Pair.lazy(lower, upper);
      }).par((par, lu) -> {
        HighestSetBitOutput lower = lu.getFirst().out();
        HighestSetBitOutput upper = lu.getSecond().out();
        DRes<SInt> anyBitSet = par.seq(seq -> {
          Numeric num = seq.numeric();
          return num.sub(num.add(lower.anyBitSet, upper.anyBitSet),
              num.mult(lower.anyBitSet, upper.anyBitSet));
        });
        DRes<SInt> index = par.par(subPar -> {
          Numeric num = subPar.numeric();
          DRes<SInt> term1 = num.add(N, upper.index);
          DRes<SInt> term2 = num.sub(1, upper.anyBitSet);
          return Pair.lazy(term1, term2);
        }).pairInPar((seq, terms) -> seq.numeric().mult(terms.getFirst(), upper.anyBitSet),
            (seq, terms) -> seq.numeric().mult(terms.getSecond(), lower.index))
            .seq((seq, terms) -> seq.numeric().add(terms.getFirst(), terms.getSecond()));

        DRes<SInt> normalizer = par.par(subPar -> {
          Numeric num = subPar.numeric();
          DRes<SInt> term1 = num.sub(1, upper.anyBitSet);
          DRes<SInt> term2 = num.sub(lower.normalizer, 1);
          return Pair.lazy(term1, term2);
        }).seq((seq, terms) -> seq.numeric().mult(upper.normalizer,
            seq.numeric().add(1, seq.numeric().mult(terms.getFirst(), terms.getSecond()))));

        return new HighestSetBitOutput(anyBitSet, index, normalizer);
      });

    }
  }
}
