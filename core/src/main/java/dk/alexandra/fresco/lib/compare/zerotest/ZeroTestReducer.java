package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.FrescoFunction;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.HammingDistance;

public class ZeroTestReducer implements FrescoFunction<SInt, SInt> {

  private final int bitLength;

  public ZeroTestReducer(int bitLength) {
    this.bitLength = bitLength;
  }

  @Override
  public Computation<SInt> apply(SInt input, SequentialProtocolBuilder builder) {
    return builder.seq((seq) ->
        seq.createAdditiveMaskBuilder().additiveMask(bitLength)
    ).seq((mask, seq) -> {
      Computation<SInt> mS = seq.numeric().add(input, mask.r);
      Computation<OInt> mO = seq.createOpenBuilder().open(mS);
      return () -> new Pair<>(mask.bits, mO.out());
    }).seq(
        new HammingDistance()
    );
  }
}
