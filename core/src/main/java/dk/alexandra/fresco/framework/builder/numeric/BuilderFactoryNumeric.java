package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;

/**
 * The core factory to implement when creating a numeric protocol. Every {@link
 * ComputationDirectory} found in this factory will append the produced protocols to the supplied
 * builder. Implementors must provide a {@link Numeric} - being directory for
 *
 * <ul>
 *   <li>simple, numeric operations (+, -, *)
 *   <li>Open operations for opening a small subset of values used in the control flow (is a<b)<
 *   <li>Factories for producing secret shared values
 * </ul>
 *
 * The other directories have defaults, based on the raw methods, but can be overridden if the
 * particular protocol suite has a more efficient way of e.g. comparing two numbers than a generic
 * approach would have.
 */
public interface BuilderFactoryNumeric extends BuilderFactory<ProtocolBuilderNumeric> {

  BasicNumericContext getBasicNumericContext();

  Numeric createNumeric(ProtocolBuilderNumeric builder);

  default PreprocessedValues createPreprocessedValues(ProtocolBuilderNumeric builder) {
    return new DefaultPreprocessedValues(builder);
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
