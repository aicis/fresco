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
    Numeric numericBuilder = builder.numeric();
    bits = new ArrayList<>();
    for (int i = 0; i < noOfBits; i++) {
      DRes<SInt> randomBit = numericBuilder.randomBit();
      bits.add(randomBit);
    }

    MiscBigIntegerGenerators oIntGenerators = new MiscBigIntegerGenerators(builder.getBasicNumericContext().getModulus());
    AdvancedNumeric advancedNumeric = AdvancedNumeric.using(builder);
    List<BigInteger> twoPows = oIntGenerators.getTwoPowersList(noOfBits);
    value = advancedNumeric.innerProductWithPublicPart(twoPows, bits);
    return () -> new AdvancedNumeric.RandomAdditiveMask(
        bits,
        value.out());
  }
}
