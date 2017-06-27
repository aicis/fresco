package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public interface NumericBuilder {

  Computation<SInt> add(Computation<SInt> a, Computation<SInt> b);

  Computation<SInt> add(OInt a, Computation<SInt> b);

  Computation<SInt> sub(Computation<SInt> a, Computation<SInt> b);

  Computation<SInt> sub(OInt a, Computation<SInt> b);

  Computation<SInt> sub(Computation<SInt> a, OInt b);

  Computation<SInt> mult(Computation<SInt> a, Computation<SInt> b);

  Computation<SInt> mult(OInt a, Computation<SInt> b);


  /**
   * Returns a protocol which creates a secret shared random bit. (This should be computed
   * beforehand)
   */
  Computation<SInt> randomBit();

  Computation<SInt> randomElement();

  Computation<OInt> invert(OInt oInt);

  Computation<SInt> known(BigInteger value);

  Computation<SInt> input(BigInteger value, int inputParty);

  Computation<OInt> open(Computation<SInt> secretShare);

  Computation<SInt[]> getExponentiationPipe();

  /**
   * Returns a clearText representation of value^1, value^2, ..., value^maxExp
   */
  Computation<OInt[]> getExpFromOInt(OInt value, int maxExp);
}
