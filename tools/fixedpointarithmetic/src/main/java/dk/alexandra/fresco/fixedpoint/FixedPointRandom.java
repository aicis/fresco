package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Construct a random SFixed with a value between 0 and 1.  
 *
 */
public class FixedPointRandom implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final int scaleSize;
  private final int precision;

  public FixedPointRandom(int precision) {
    this.precision = precision;
    this.scaleSize = (int)Math.ceil((Math.log(Math.pow(10, precision))/(Math.log(2))));
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<RandomAdditiveMask> random = seq.advancedNumeric().additiveMask(scaleSize);
      return () -> random;
    }).seq((seq, random) -> {
      
      DRes<SInt> rand = random.out().r;
      BigInteger divi = BigInteger.valueOf(2).pow(scaleSize);
      DRes<SInt> r2 = seq.numeric().mult(BigInteger.TEN.pow(precision), rand);
      DRes<SInt> result = seq.advancedNumeric().div(r2, divi);
      
      return () -> new SFixedSIntWrapper(result);
    });
  }
}