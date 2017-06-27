package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public interface InputBuilder {

  Computation<SInt> known(BigInteger value);

  Computation<SInt> input(BigInteger value, int inputParty);

}
