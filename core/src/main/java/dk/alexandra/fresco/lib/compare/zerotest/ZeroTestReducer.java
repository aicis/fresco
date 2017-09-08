package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.HammingDistance;
import java.math.BigInteger;

public class ZeroTestReducer implements Computation<SInt, ProtocolBuilderNumeric> {

  private final int bitLength;
  private final DRes<SInt> input;

  public ZeroTestReducer(int bitLength, DRes<SInt> input) {
    this.bitLength = bitLength;
    this.input = input;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) -> seq.advancedNumeric().additiveMask(bitLength)
    ).seq((seq, mask) -> {
      DRes<SInt> mS = seq.numeric().add(input, () -> mask.r);
      DRes<BigInteger> mO = seq.numeric().open(mS);
      return () -> new Pair<>(mask.bits, mO.out());
    }).seq((seq, pair) ->
        new HammingDistance(pair.getFirst(), pair.getSecond()).buildComputation(seq)
    );
  }
}
