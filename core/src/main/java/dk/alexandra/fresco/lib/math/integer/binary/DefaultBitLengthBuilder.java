package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.BitLengthBuilder;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;

public class DefaultBitLengthBuilder implements BitLengthBuilder {

  private final ProtocolBuilder builder;

  public DefaultBitLengthBuilder(ProtocolBuilder builder) {
    this.builder = builder;
  }

  @Override
  public Computation<SInt> bitLength(Computation<SInt> input, int maxBitLength) {
    return builder.createSequentialSub(
        new BitLengthProtocol4(input, maxBitLength));

  }
}
