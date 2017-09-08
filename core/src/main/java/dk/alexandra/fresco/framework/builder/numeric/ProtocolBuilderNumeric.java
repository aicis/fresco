package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;

/**
 * Central class for building protocols that are based on numeric protocols.
 */
public class ProtocolBuilderNumeric extends
    ProtocolBuilderImpl<ProtocolBuilderNumeric> {

  private final BuilderFactoryNumeric factory;
  private BasicNumericContext basicNumericContext;
  private Numeric numeric;
  private Comparison comparison;
  private AdvancedNumeric advancedNumeric;
  private Debug debug;

  ProtocolBuilderNumeric(BuilderFactoryNumeric factory, boolean parallel) {
    super(factory, parallel);
    this.factory = factory;
    this.basicNumericContext = factory.getBasicNumericContext();
  }

  public BasicNumericContext getBasicNumericContext() {
    return basicNumericContext;
  }

  /**
   * Creates a numeric builder for this instance - i.e. this intended producer.
   *
   * @return the numeric builder.
   */
  public Numeric numeric() {
    if (numeric == null) {
      numeric = factory.createNumeric(this);
    }
    return numeric;
  }

  /**
   * Creates a comparison builder for this instance - i.e. this intended producer.
   *
   * @return the comparison builder.
   */
  public Comparison comparison() {
    if (comparison == null) {
      comparison = factory.createComparison(this);
    }
    return comparison;
  }

  public AdvancedNumeric advancedNumeric() {
    if (advancedNumeric == null) {
      advancedNumeric = factory.createAdvancedNumeric(this);
    }
    return advancedNumeric;
  }

  public Debug debug() {
    if (debug == null) {
      debug = factory.createDebug(this);
    }
    return debug;
  }

  public MiscOIntGenerators getBigIntegerHelper() {
    return factory.getBigIntegerHelper();
  }
}
