package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;

/**
 * The core factory to implement when creating a binary protocol. Every subbuilder from this
 * factory must be builders and append to the supplied builder. Implementors must provide builders
 * for <ul> <li>simple, binary operations (and, xor, not)</li> <li>Open operations for opening a small
 * subset of values used in the control flow (is a<b)<</li> <li>Factories for producing secret
 * shared values</li> </ul> Other builders have defaults, based on the raw methods, but can be
 * overridden.
 */
public interface BuilderFactoryBinary extends BuilderFactory<SequentialBinaryBuilder> {

  BasicLogicFactory getBasicLogicFactory();
 
  BinaryBuilder createBinaryBuilder(ProtocolBuilderBinary builder);
  
  @Override
  default SequentialBinaryBuilder createProtocolBuilder() {
    return ProtocolBuilderBinary.createApplicationRoot(this);
  }
  
}
