package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;

/**
 * Central class for building protocols that are based on numeric protocol suites. This class
 * contains all computation directories within FRESCO which numeric applications can use to build
 * applications.
 */
public class ProtocolBuilderNumeric extends ProtocolBuilderImpl<ProtocolBuilderNumeric> {

  private final BuilderFactoryNumeric factory;
  private BasicNumericContext basicNumericContext;
  private Numeric numeric;
  private PreprocessedValues preprocessedValues;
  ProtocolBuilderNumeric(BuilderFactoryNumeric factory, boolean parallel) {
    super(factory, parallel);
    this.factory = factory;
    this.basicNumericContext = factory.getBasicNumericContext();
  }

  /**
   * Returns the container for information about the field of operation.
   * 
   * @return The {@link BasicNumericContext} used within this protocol builder.
   */
  public BasicNumericContext getBasicNumericContext() {
    return basicNumericContext;
  }
  

  /**
   * Creates a {@link Numeric} computation directory for this instance - i.e. this intended
   * producer. Contains only protocol suite native basic operations such as Addition and
   * multiplication.
   * 
   * @return The {@link Numeric} computation directory.
   */
  public Numeric numeric() {
    if (numeric == null) {
      numeric = factory.createNumeric(this);
    }
    return numeric;
  }

  /**
   * Creates a {@link PreprocessedValues} computation directory for this instance - i.e. this
   * intended producer. Contains elements which, if created prior to this evaluation, would save
   * computation and network. Preprocessed values does not depend on the input of the function to
   * evaluate.
   * 
   * @return The preprocessed values computation directory.
   */
  public PreprocessedValues preprocessedValues() {
    if (preprocessedValues == null) {
      preprocessedValues = factory.createPreprocessedValues(this);
    }
    return preprocessedValues;
  }

}
