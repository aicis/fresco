package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.DRes;

/**
 * Any computation in fresco being composite by nature - and hence requires a specific
 * type of builder.
 * @param <InputT> the type of input of this function
 * @param <BuilderT> the type of builder of the composition
 * @param <OutputT> the type of output of the function
 */
public interface FrescoLambda<
    InputT,
    BuilderT extends ProtocolBuilder,
    OutputT
    > {

  /**
   * Builds the computation from the given input.
   *
   * @param builder the builder that allows the creation of subsequent (native) protocols
   * @param input the input to this computation
   * @return the computation as the result
   */
  DRes<OutputT> buildComputation(BuilderT builder, InputT input);

}
