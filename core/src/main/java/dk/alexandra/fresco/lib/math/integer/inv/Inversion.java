package dk.alexandra.fresco.lib.math.integer.inv;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Computes the inverse of an element within the field of operation. 
 */
public class Inversion implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> x;

  public Inversion(DRes<SInt> x) {
    this.x = x;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    Numeric numeric = builder.numeric();
    DRes<SInt> random = numeric.randomElement();
    DRes<SInt> sProduct = numeric.mult(x, random);
    DRes<BigInteger> open = numeric.open(sProduct);
    return builder.seq((seq) -> {
      BigInteger value = open.out();
      BigInteger inverse = value.modInverse(seq.getBasicNumericContext().getModulus());
      return seq.numeric().mult(inverse, random);
    });
  }
}
