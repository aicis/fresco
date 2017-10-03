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

  private final int securityParameter;
  private final int noOfBits;

  private List<DRes<SInt>> bits;
  private DRes<SInt> value;

  public RandomAdditiveMask(int securityParameter, int noOfBits) {
    this.securityParameter = securityParameter;
    this.noOfBits = noOfBits;
  }

  @Override
  public DRes<AdvancedNumeric.RandomAdditiveMask> buildComputation(
      ProtocolBuilderNumeric builder) {
    Numeric numericBuilder = builder.numeric();
    List<DRes<SInt>> allBits = new ArrayList<>();
    for (int i = 0; i < noOfBits + securityParameter; i++) {
      DRes<SInt> randomBit = numericBuilder.randomBit();
      allBits.add(randomBit);
    }

    MiscBigIntegerGenerators oIntGenerators = builder.getBigIntegerHelper();

    List<BigInteger> twoPows = oIntGenerators.getTwoPowersList(securityParameter + noOfBits);
    AdvancedNumeric innerProductBuilder = builder.advancedNumeric();
    value = innerProductBuilder.innerProductWithPublicPart(twoPows, allBits);
    bits = allBits.subList(0, noOfBits);
    return () -> new AdvancedNumeric.RandomAdditiveMask(
        bits,
        value.out());
  }
}
