package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.value.SInt;

public interface BitLengthBuilder {

  Computation<SInt> bitLength(Computation<SInt> input, int maxBitLength);

}
