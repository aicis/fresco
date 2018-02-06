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

  private final DRes<SInt> value;

  public Inversion(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    Numeric numeric = builder.numeric();
    DRes<SInt> random = numeric.randomElement();
    DRes<SInt> product = numeric.mult(value, random);
    DRes<BigInteger> open = numeric.open(product);
    return builder.seq((seq) -> {
      BigInteger value = open.out();
      BigInteger inverse = value.modInverse(seq.getBasicNumericContext().getModulus());
      return seq.numeric().mult(inverse, random);
    });
  }
}
