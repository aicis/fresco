package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public interface NumericBuilder {

  Computation<SInt> add(Computation<SInt> a, Computation<SInt> b);

  Computation<SInt> add(BigInteger a, Computation<SInt> b);

  Computation<SInt> sub(Computation<SInt> a, Computation<SInt> b);

  Computation<SInt> sub(BigInteger a, Computation<SInt> b);

  Computation<SInt> sub(Computation<SInt> a, BigInteger b);

  Computation<SInt> mult(Computation<SInt> a, Computation<SInt> b);

  Computation<SInt> mult(BigInteger a, Computation<SInt> b);


  /**
   * Returns a protocol which creates a secret shared random bit. (This should be computed
   * beforehand)
   */
  Computation<SInt> randomBit();

  Computation<SInt> randomElement();

  Computation<SInt> known(BigInteger value);

  Computation<SInt> input(BigInteger value, int inputParty);

  Computation<BigInteger> open(Computation<SInt> secretShare);

  Computation<BigInteger> open(Computation<SInt> secretShare, int outputParty);

  Computation<SInt[]> getExponentiationPipe();

}
