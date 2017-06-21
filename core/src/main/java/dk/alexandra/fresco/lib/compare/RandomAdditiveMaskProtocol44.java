package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
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
import java.util.stream.Collectors;

public class RandomAdditiveMaskProtocol44<SIntT extends SInt>
    implements Function<SequentialProtocolBuilder<SIntT>, Computation<RandomAdditiveMask<SIntT>>> {

  private final BuilderFactoryNumeric<SIntT> factoryNumeric;
  private final int securityParameter;
  private final int noOfBits;

  private List<Computation<SIntT>> bits;
  private Computation<SIntT> value;

  public RandomAdditiveMaskProtocol44(BuilderFactoryNumeric<SIntT> factoryNumeric,
      int securityParameter, int noOfBits) {
    this.factoryNumeric = factoryNumeric;
    this.securityParameter = securityParameter;
    this.noOfBits = noOfBits;
  }

  @Override
  public Computation<RandomAdditiveMask<SIntT>> apply(SequentialProtocolBuilder<SIntT> builder) {
    NumericBuilder<SIntT> numericBuilder = builder.createNumericBuilder();
    List<Computation<SIntT>> allBits = new ArrayList<>();
    for (int i = 0; i < noOfBits + securityParameter; i++) {
      Computation<SIntT> randomBit = numericBuilder.createRandomSecretSharedBitProtocol();
      allBits.add(randomBit);
    }

    MiscOIntGenerators oIntGenerators = new MiscOIntGenerators(
        factoryNumeric.getBasicNumericFactory());

    OInt[] twoPows = oIntGenerators.getTwoPowers(securityParameter + noOfBits);
    InnerProductBuilder<SIntT> innerProductBuilder = builder.createInnerProductBuilder();
    value = innerProductBuilder.openDot(Arrays.asList(twoPows), allBits);
    bits = allBits.subList(0, noOfBits);
    return () -> new RandomAdditiveMask<>(
        bits.stream().map(Computation::out).collect(Collectors.toList()),
        value.out());
  }
}
