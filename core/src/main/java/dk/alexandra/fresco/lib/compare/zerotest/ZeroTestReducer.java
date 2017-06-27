package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.FrescoFunction;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.HammingDistance;

public class ZeroTestReducer implements FrescoFunction<SInt> {

  private final int bitLength;
  private final Computation<SInt> input;

  public ZeroTestReducer(int bitLength, Computation<SInt> input) {
    this.bitLength = bitLength;
    this.input = input;
  }

  @Override
  public Computation<SInt> apply(SequentialProtocolBuilder builder) {
    return builder.seq((seq) ->
        seq.createAdditiveMaskBuilder().additiveMask(bitLength)
    ).seq((mask, seq) -> {
      Computation<SInt> mS = seq.numeric().add(input, mask.r);
      Computation<OInt> mO = seq.createOpenBuilder().open(mS);
      return () -> new Pair<>(mask.bits, mO.out());
    }).seq((pair, seq) ->
        new HammingDistance(pair.getFirst(), pair.getSecond()).apply(seq)
    );
  }
}
