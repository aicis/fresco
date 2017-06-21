package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthProtocol4;

public class DefaultBitLengthBuilder implements BitLengthBuilder {

  private final ProtocolBuilder<SInt> builder;

  DefaultBitLengthBuilder(ProtocolBuilder<SInt> builder) {
    this.builder = builder;
  }

  @Override
  public Computation<SInt> bitLength(Computation<SInt> input, int maxBitLength) {
    return builder.createSequentialSubFactoryReturning(
        new BitLengthProtocol4(input, maxBitLength));

  }
}
