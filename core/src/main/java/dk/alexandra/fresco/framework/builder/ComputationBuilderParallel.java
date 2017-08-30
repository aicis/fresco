package dk.alexandra.fresco.framework.builder;

/**
 * Marker interface for the computations that can be executed in parallel/batches - they can
 * actually be run in sequence, but would perform better in parallel.
 *
 * @param <BuilderT> the type of builder of the composition
 * @param <OutputT> the type of output of the function
 */
public interface ComputationBuilderParallel<OutputT, BuilderT extends ProtocolBuilder>
    extends ComputationBuilder<OutputT, BuilderT> {

}
