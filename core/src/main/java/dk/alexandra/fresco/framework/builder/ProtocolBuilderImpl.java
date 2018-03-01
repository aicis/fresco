package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.util.LinkedList;
import java.util.List;

public abstract class ProtocolBuilderImpl<BuilderT extends ProtocolBuilderImpl<BuilderT>>
    implements ProtocolBuilder {

  private final boolean parallel;
  private List<ProtocolProducer> protocols;
  private BuilderFactory<BuilderT> factory;

  protected ProtocolBuilderImpl(
      BuilderFactory<BuilderT> factory,
      boolean parallel) {
    this.parallel = parallel;
    this.protocols = new LinkedList<>();
    this.factory = factory;
  }

  private void createAndAppend(ProtocolProducer producer) {
    if (protocols == null) {
      throw new IllegalStateException("Cannot build this twice, it has all ready been constructed");
    }
    protocols.add(producer);
  }

  /**
   * Appends a concrete, native protocol to the list of producers - useful for the native protocol
   * factories that needs to be builders.
   *
   * @param nativeProtocol the native protocol to add
   * @param <T> the result type of the native protocol
   * @return a computation that resolves to the result of the native protocol once evaluated
   */
  public <T> DRes<T> append(NativeProtocol<T, ?> nativeProtocol) {
    SingleProtocolProducer<T> producer = new SingleProtocolProducer<>(nativeProtocol);
    createAndAppend(producer);
    return producer;
  }

  /**
   * Building the actual protocol producer. Implementors decide which producer to create.
   *
   * @return the protocol producer that has been build
   */
  public ProtocolProducer build() {
    if (parallel) {
      ParallelProtocolProducer parallelProtocolProducer = new ParallelProtocolProducer(protocols);
      protocols = null;
      return parallelProtocolProducer;
    } else {
      SequentialProtocolProducer sequentialProtocolProducer =
          new SequentialProtocolProducer(protocols);
      protocols = null;
      return sequentialProtocolProducer;
    }
  }

  /**
   * Creates a new Build step based on this builder but with a nested sequential protocol
   * producer inserted into the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <R> BuildStep<Void, BuilderT, R> seq(Computation<R, BuilderT> function) {
    FrescoLambda<Void, BuilderT, R> innerBuilder =
        (inner, ignored) -> function.buildComputation(inner);
    BuildStep<Void, BuilderT, R> builder =
        new BuildStep<>(new BuildStepSingle<>(innerBuilder, false));
    createAndAppend(new LazyProtocolProducerDecorator(() -> builder.createProducer(null, factory)));
    return builder;
  }

  /**
   * Creates a new Build step based on this builder but with a nested parallel protocol
   * producer inserted into the original protocol producer.
   *
   * @param f of the protocol producer - will be lazy evaluated
   */
  public <R> BuildStep<Void, BuilderT, R> par(ComputationParallel<R, BuilderT> f) {
    FrescoLambda<Void, BuilderT, R> innerBuilder = (inner, ignored) -> f.buildComputation(inner);
    BuildStep<Void, BuilderT, R> builder =
        new BuildStep<>(new BuildStepSingle<>(innerBuilder, true));
    createAndAppend(new LazyProtocolProducerDecorator(() -> builder.createProducer(null, factory)));
    return builder;
  }
}
