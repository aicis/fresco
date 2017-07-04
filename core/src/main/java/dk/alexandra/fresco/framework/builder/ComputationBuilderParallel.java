package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;

public interface ComputationBuilderParallel<OutputT> {

  /**
   * Applies this function to the given argument.
   *
   * @param builder the function argument
   * @return the function result
   */
  Computation<OutputT> build(ParallelNumericBuilder builder);

}
