package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

  default List<Computation<SInt>> known(List<BigInteger> value) {
    List<Computation<SInt>> res = new ArrayList<>();
    for (BigInteger b : value) {
      res.add(known(b));
    }
    return res;
  }

  Computation<SInt> input(BigInteger value, int inputParty);

  Computation<BigInteger> open(Computation<SInt> secretShare);

  Computation<SInt[]> getExponentiationPipe();

}
