package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;

/**
 * Root computation in fresco being composite by nature - and hence requires a specific
 * type of builder. This is similar to {@link FrescoLambda} but without input.
 *
 * @param <BuilderT> the type of builder of the composition
 * @param <OutputT> the type of output of the function
 */
public interface ComputationBuilder<OutputT, BuilderT extends ProtocolBuilder> {

  /**
   * Builds the computation from the Builder. The builder is the root scope and allows for new
   * sub computations to be created either via native protocols or via other composite
   * ComputationBuilder.
   *
   * @param builder the builder that allows the creation of subsequent (native) protocols
   * @return the computation as the result
   */
  Computation<OutputT> buildComputation(BuilderT builder);

}
