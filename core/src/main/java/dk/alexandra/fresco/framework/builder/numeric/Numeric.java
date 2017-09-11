package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public interface Numeric extends ComputationDirectory {

  DRes<SInt> add(DRes<SInt> a, DRes<SInt> b);

  DRes<SInt> add(BigInteger a, DRes<SInt> b);

  DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b);

  DRes<SInt> sub(BigInteger a, DRes<SInt> b);

  DRes<SInt> sub(DRes<SInt> a, BigInteger b);

  DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b);

  DRes<SInt> mult(BigInteger a, DRes<SInt> b);


  /**
   * Returns a protocol which creates a secret shared random bit. (This should be computed
   * beforehand)
   */
  DRes<SInt> randomBit();

  DRes<SInt> randomElement();

  DRes<SInt> known(BigInteger value);

  DRes<SInt> input(BigInteger value, int inputParty);

  DRes<BigInteger> open(DRes<SInt> secretShare);

  DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty);

  DRes<SInt[]> getExponentiationPipe();

}
