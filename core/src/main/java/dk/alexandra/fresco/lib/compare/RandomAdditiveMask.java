package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
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

    MiscBigIntegerGenerators oIntGenerators = builder.getBigIntegerHelper();

    List<BigInteger> twoPows = oIntGenerators.getTwoPowersList(noOfBits);
    AdvancedNumeric innerProductBuilder = builder.advancedNumeric();
    value = innerProductBuilder.innerProductWithPublicPart(twoPows, bits);
    return () -> new AdvancedNumeric.RandomAdditiveMask(
        bits,
        value.out());
  }
}
