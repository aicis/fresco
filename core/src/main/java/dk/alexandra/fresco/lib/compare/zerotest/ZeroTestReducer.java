package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.HammingDistance;
import java.math.BigInteger;

public class ZeroTestReducer implements ComputationBuilder<SInt, ProtocolBuilderNumeric> {

  private final int bitLength;
  private final Computation<SInt> input;

  public ZeroTestReducer(int bitLength, Computation<SInt> input) {
    this.bitLength = bitLength;
    this.input = input;
  }

  @Override
  public Computation<SInt> build(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) -> seq.advancedNumeric().additiveMask(bitLength)
    ).seq((mask, seq) -> {
      Computation<SInt> mS = seq.numeric().add(input, () -> mask.r);
      Computation<BigInteger> mO = seq.numeric().open(mS);
      return () -> new Pair<>(mask.bits, mO.out());
    }).seq((pair, seq) ->
        new HammingDistance(pair.getFirst(), pair.getSecond()).build(seq)
    );
  }
}
