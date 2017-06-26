package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BitLengthBuilder;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.RightShiftBuilder;
import dk.alexandra.fresco.lib.compare.DefaultComparisonBuilder;
import dk.alexandra.fresco.lib.compare.DefaultRandomAdditiveMaskBuilder;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.binary.DefaultBitLengthBuilder;
import dk.alexandra.fresco.lib.math.integer.binary.DefaultRightShiftBuilder;
import dk.alexandra.fresco.lib.math.integer.division.DefaultAdvancedNumericBuilder;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.DefaultInnerProductBuilder;

/**
 * The core factory to implement when creating a numeric protocol. Every subbuilder from this
 * factory must be builders and append to the supplied builder. Implementors must provide builders
 * for <ul> <li>simple, numeric operations (+, -, *)</li> <li>Open operations for opening a small
 * subset of values used in the control flow (is a<b)<</li> <li>Factories for producing secret
 * shared values</li> </ul> Other builders have defaults, based on the raw methods, but can be
 * overridden.
 */
public interface BuilderFactoryNumeric extends BuilderFactory {

  int MAGIC_SECURE_NUMBER = 60;

  BasicNumericFactory getBasicNumericFactory();

  NumericBuilder createNumericBuilder(ProtocolBuilder builder);

  OpenBuilder createOpenBuilder(ProtocolBuilder builder);

  InputBuilder createInputBuilder(ProtocolBuilder builder);


  default PreprocessedExpPipeFactory getPreprocessedExpPipe() {
    return (PreprocessedExpPipeFactory) getBasicNumericFactory();
  }

  default ExpFromOIntFactory getExpFromOInt() {
    return (ExpFromOIntFactory) getBasicNumericFactory();
  }

  default ComparisonBuilder createComparisonBuilder(ProtocolBuilder builder) {
    return new DefaultComparisonBuilder(this, builder);
  }

  default InnerProductBuilder createInnerProductBuilder(ProtocolBuilder builder) {
    return new DefaultInnerProductBuilder(builder);
  }

  default RandomAdditiveMaskBuilder createAdditiveMaskBuilder(
      ProtocolBuilder builder) {
    return new DefaultRandomAdditiveMaskBuilder(this, builder, MAGIC_SECURE_NUMBER);
  }

  default RightShiftBuilder createRightShiftBuilder(
      ProtocolBuilder builder) {
    return new DefaultRightShiftBuilder(this, builder);
  }

  default AdvancedNumericBuilder createAdvancedNumericBuilder(ProtocolBuilder builder) {
    return new DefaultAdvancedNumericBuilder(this, builder);
  }

  default BitLengthBuilder createBitLengthBuilder(ProtocolBuilder builder) {
    return new DefaultBitLengthBuilder(builder);
  }

}
