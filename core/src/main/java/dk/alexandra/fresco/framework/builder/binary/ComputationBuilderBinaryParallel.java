package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.ParallelBinaryBuilder;

public interface ComputationBuilderBinaryParallel<OutputT> {

  /**
   * Applies this function to the given argument.
   *
   * @param builder the function argument
   * @return the function result
   */
  Computation<OutputT> build(ParallelBinaryBuilder builder);
}
