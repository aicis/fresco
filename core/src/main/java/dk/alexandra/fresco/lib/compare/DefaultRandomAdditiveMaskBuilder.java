package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.RandomAdditiveMaskBuilder;

public class DefaultRandomAdditiveMaskBuilder implements
    RandomAdditiveMaskBuilder {

  private final BuilderFactoryNumeric factoryNumeric;
  private final ProtocolBuilder builder;
  private final int securityParameter;

  public DefaultRandomAdditiveMaskBuilder(BuilderFactoryNumeric factoryNumeric,
      ProtocolBuilder builder,
      int securityParameter) {
    this.factoryNumeric = factoryNumeric;
    this.builder = builder;
    this.securityParameter = securityParameter;
  }

  @Override
  public Computation<RandomAdditiveMask> additiveMask(int noOfBits) {
    return builder
        .createSequentialSubFactoryReturning(
            new RandomAdditiveMaskProtocol44(factoryNumeric, securityParameter, noOfBits));
  }

}
