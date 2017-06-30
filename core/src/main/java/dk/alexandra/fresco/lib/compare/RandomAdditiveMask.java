package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RandomAdditiveMask implements
    ComputationBuilder<AdvancedNumericBuilder.RandomAdditiveMask> {

  private final BuilderFactoryNumeric factoryNumeric;
  private final int securityParameter;
  private final int noOfBits;

  private List<Computation<SInt>> bits;
  private Computation<SInt> value;

  public RandomAdditiveMask(BuilderFactoryNumeric factoryNumeric,
      int securityParameter, int noOfBits) {
    this.factoryNumeric = factoryNumeric;
    this.securityParameter = securityParameter;
    this.noOfBits = noOfBits;
  }

  @Override
  public Computation<AdvancedNumericBuilder.RandomAdditiveMask> build(
      SequentialProtocolBuilder builder) {
    NumericBuilder numericBuilder = builder.numeric();
    List<Computation<SInt>> allBits = new ArrayList<>();
    for (int i = 0; i < noOfBits + securityParameter; i++) {
      Computation<SInt> randomBit = numericBuilder.randomBit();
      allBits.add(randomBit);
    }

    MiscOIntGenerators oIntGenerators = builder.getBigIntegerHelper();

    List<BigInteger> twoPows = oIntGenerators.getTwoPowersList(securityParameter + noOfBits);
    AdvancedNumericBuilder innerProductBuilder = builder.createAdvancedNumericBuilder();
    value = innerProductBuilder.openDot(twoPows, allBits);
    bits = allBits.subList(0, noOfBits);
    return () -> new AdvancedNumericBuilder.RandomAdditiveMask(
        bits,
        value.out());
  }
}
