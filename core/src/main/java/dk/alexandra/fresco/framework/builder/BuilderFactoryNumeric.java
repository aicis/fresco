package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.lib.compare.DefaultComparisonBuilder;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.division.DefaultAdvancedNumericBuilder;

/**
 * The core factory to implement when creating a numeric protocol. Every subbuilder from this
 * factory must be builders and append to the supplied builder. Implementors must provide builders
 * for <ul> <li>simple, numeric operations (+, -, *)</li> <li>Open operations for opening a small
 * subset of values used in the control flow (is a<b)<</li> <li>Factories for producing secret
 * shared values</li> </ul> Other builders have defaults, based on the raw methods, but can be
 * overridden.
 */
public interface BuilderFactoryNumeric extends BuilderFactory<SequentialProtocolBuilder> {

  int MAGIC_SECURE_NUMBER = 60;

  BasicNumericFactory getBasicNumericFactory();

  NumericBuilder createNumericBuilder(ProtocolBuilderNumeric builder);

  MiscOIntGenerators getBigIntegerHelper();

  default ComparisonBuilder createComparisonBuilder(ProtocolBuilderNumeric builder) {
    return new DefaultComparisonBuilder(this, builder);
  }

  default AdvancedNumericBuilder createAdvancedNumericBuilder(ProtocolBuilderNumeric builder) {
    return new DefaultAdvancedNumericBuilder(this, builder);
  }

  @Override
  default SequentialProtocolBuilder createProtocolBuilder() {
    return ProtocolBuilderNumeric.createApplicationRoot(this);
  }
}
