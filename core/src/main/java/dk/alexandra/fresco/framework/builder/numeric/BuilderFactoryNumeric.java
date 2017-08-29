package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumeric;

/**
 * The core factory to implement when creating a numeric protocol. Every subbuilder from this
 * factory must be builders and append to the supplied builder. Implementors must provide builders
 * for
 * <ul>
 * <li>simple, numeric operations (+, -, *)</li>
 * <li>Open operations for opening a small subset of values used in the control flow (is a<b)<</li>
 * <li>Factories for producing secret shared values</li>
 * </ul>
 * Other builders have defaults, based on the raw methods, but can be overridden.
 */
public interface BuilderFactoryNumeric extends BuilderFactory<ProtocolBuilderNumeric> {

  int MAGIC_SECURE_NUMBER = 60;

  BasicNumeric getBasicNumericFactory();

  NumericBuilder createNumericBuilder(ProtocolBuilderNumeric builder);

  MiscOIntGenerators getBigIntegerHelper();

  default ComparisonBuilder createComparison(ProtocolBuilderNumeric builder) {
    return new DefaultComparisonBuilder(this, builder);
  }

  default AdvancedNumericBuilder createAdvancedNumeric(ProtocolBuilderNumeric builder) {
    return new DefaultAdvancedNumericBuilder(this, builder);
  }

  /**
   * Returns a builder which can be helpful while developing a new protocol. Be very careful though,
   * to include this in any production code since the debugging opens values to all parties.
   * 
   * @param builder
   * @return By default a standard debugger which opens values and prints them.
   */
  default DebugBuilder createDebugBuilder(ProtocolBuilderNumeric builder) {
    return new DefaultDebugBuilder(builder);
  }

  @Override
  default ProtocolBuilderNumeric createSequential() {
    return new ProtocolBuilderNumeric(this, false);
  }

  @Override
  default ProtocolBuilderNumeric createParallel() {
    return new ProtocolBuilderNumeric(this, true);
  }
}
