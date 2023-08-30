package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RandomAdditiveMask implements
    Computation<AdvancedNumeric.RandomAdditiveMask, ProtocolBuilderNumeric> {

  private final int noOfBits;
  private List<DRes<SInt>> bits;
  private DRes<SInt> value;

  public RandomAdditiveMask(int noOfBits) {
    this.noOfBits = noOfBits;
  }

  @Override
  public DRes<AdvancedNumeric.RandomAdditiveMask> buildComputation(
          ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      Numeric numericBuilder = par.numeric();
      bits = new ArrayList<>();
      for (int i = 0; i < noOfBits; i++) {
        DRes<SInt> randomBit = numericBuilder.randomBit();
        bits.add(randomBit);
      }
      return DRes.of(bits);
    }).seq((seq, bits) -> {
      MiscBigIntegerGenerators oIntGenerators = new MiscBigIntegerGenerators(seq.getBasicNumericContext().getModulus());
      List<BigInteger> twoPows = oIntGenerators.getTwoPowersList(noOfBits);
      value = AdvancedNumeric.using(seq).innerProductWithPublicPart(twoPows, bits);
      return DRes.of(new AdvancedNumeric.RandomAdditiveMask(bits, value.out()));
    });
  }
}
