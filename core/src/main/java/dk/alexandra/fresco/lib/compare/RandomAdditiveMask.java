package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RandomAdditiveMask implements
    ComputationBuilder<AdvancedNumericBuilder.RandomAdditiveMask, ProtocolBuilderNumeric> {

  private final int securityParameter;
  private final int noOfBits;

  private List<Computation<SInt>> bits;
  private Computation<SInt> value;

  public RandomAdditiveMask(int securityParameter, int noOfBits) {
    this.securityParameter = securityParameter;
    this.noOfBits = noOfBits;
  }

  @Override
  public Computation<AdvancedNumericBuilder.RandomAdditiveMask> buildComputation(
      ProtocolBuilderNumeric builder) {
    NumericBuilder numericBuilder = builder.numeric();
    List<Computation<SInt>> allBits = new ArrayList<>();
    for (int i = 0; i < noOfBits + securityParameter; i++) {
      Computation<SInt> randomBit = numericBuilder.randomBit();
      allBits.add(randomBit);
    }

    MiscOIntGenerators oIntGenerators = builder.getBigIntegerHelper();

    List<BigInteger> twoPows = oIntGenerators.getTwoPowersList(securityParameter + noOfBits);
    AdvancedNumericBuilder innerProductBuilder = builder.advancedNumeric();
    value = innerProductBuilder.openDot(twoPows, allBits);
    bits = allBits.subList(0, noOfBits);
    return () -> new AdvancedNumericBuilder.RandomAdditiveMask(
        bits,
        value.out());
  }
}
