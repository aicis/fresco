package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;

public interface ComputationBuilder<OutputT, SequentialBuilderT extends ProtocolBuilder<SequentialBuilderT>> {

  /**
   * Applies this function to the given argument.
   *
   * @param builder the function argument
   * @return the function result
   */
  Computation<OutputT> build(SequentialBuilderT builder);

}
