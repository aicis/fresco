package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.InnerProductBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.builder.RandomAdditiveMaskBuilder.RandomAdditiveMask;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class RandomAdditiveMaskProtocol44
    implements Function<SequentialProtocolBuilder, Computation<RandomAdditiveMask>> {

  private final BuilderFactoryNumeric factoryNumeric;
  private final int securityParameter;
  private final int noOfBits;

  private List<Computation<SInt>> bits;
  private Computation<SInt> value;

  RandomAdditiveMaskProtocol44(BuilderFactoryNumeric factoryNumeric,
      int securityParameter, int noOfBits) {
    this.factoryNumeric = factoryNumeric;
    this.securityParameter = securityParameter;
    this.noOfBits = noOfBits;
  }

  @Override
  public Computation<RandomAdditiveMask> apply(SequentialProtocolBuilder builder) {
    NumericBuilder numericBuilder = builder.numeric();
    List<Computation<SInt>> allBits = new ArrayList<>();
    for (int i = 0; i < noOfBits + securityParameter; i++) {
      Computation<SInt> randomBit = numericBuilder.createRandomSecretSharedBitProtocol();
      allBits.add(randomBit);
    }

    MiscOIntGenerators oIntGenerators = new MiscOIntGenerators(
        factoryNumeric.getBasicNumericFactory());

    OInt[] twoPows = oIntGenerators.getTwoPowers(securityParameter + noOfBits);
    InnerProductBuilder innerProductBuilder = builder.createInnerProductBuilder();
    value = innerProductBuilder.openDot(Arrays.asList(twoPows), allBits);
    bits = allBits.subList(0, noOfBits);
    return () -> new RandomAdditiveMask(
        bits,
        value.out());
  }
}
