package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.RandomAdditiveMaskBuilder;
import dk.alexandra.fresco.framework.value.SInt;

public class DefaultRandomAdditiveMaskBuilder<SIntT extends SInt> implements
    RandomAdditiveMaskBuilder<SIntT> {

  private final BuilderFactoryNumeric<SIntT> factoryNumeric;
  private final ProtocolBuilder<SIntT> builder;
  private final int securityParameter;

  public DefaultRandomAdditiveMaskBuilder(BuilderFactoryNumeric<SIntT> factoryNumeric,
      ProtocolBuilder<SIntT> builder,
      int securityParameter) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
    this.securityParameter = securityParameter;
  }

  @Override
  public Computation<RandomAdditiveMask<SIntT>> additiveMask(int noOfBits) {
    return builder
        .createSequentialSubFactoryReturning(
            new RandomAdditiveMaskProtocol44<>(factoryNumeric, securityParameter, noOfBits));
  }

}
