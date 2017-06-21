package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

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
  Computation<SInt> createRandomSecretSharedBitProtocol();

  Computation<OInt> invert(OInt oInt);
}
