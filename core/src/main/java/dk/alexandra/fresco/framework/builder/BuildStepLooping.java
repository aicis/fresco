package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import java.util.function.Predicate;

class BuildStepLooping<BuilderT extends ProtocolBuilderImpl<BuilderT>, InputT>
    implements BuildStep.NextStepBuilder<BuilderT, InputT, InputT> {


  private final Predicate<InputT> predicate;
  private final FrescoLambda<InputT, BuilderT, InputT> function;

  BuildStepLooping(Predicate<InputT> predicate, FrescoLambda<InputT, BuilderT, InputT> function) {
    super();
    this.predicate = predicate;
    this.function = function;
  }

  public Pair<ProtocolProducer, DRes<InputT>> createNextStep(
      InputT input,
      BuilderFactory<BuilderT> factory,
      BuildStep<InputT, BuilderT, ?> next) {
    LoopProtocolProducer<BuilderT, InputT> loopProtocolProducer =
        new LoopProtocolProducer<>(factory, input, predicate, function, next);
    return new Pair<>(loopProtocolProducer, loopProtocolProducer);
  }

  private static class LoopProtocolProducer<
      BuilderT extends ProtocolBuilderImpl<BuilderT>,
      InputT
      > implements ProtocolProducer, DRes<InputT> {

    private final BuilderFactory<BuilderT> factory;
    private boolean isDone;
    private boolean doneWithOwn;
    private DRes<InputT> currentResult;
    private ProtocolProducer currentProducer;
    private Predicate<InputT> predicate;
    private FrescoLambda<InputT, BuilderT, InputT> function;
    private BuildStep<InputT, BuilderT, ?> next;

    LoopProtocolProducer(
        BuilderFactory<BuilderT> factory,
        InputT input,
        Predicate<InputT> predicate,
        FrescoLambda<InputT, BuilderT, InputT> function,
        BuildStep<InputT, BuilderT, ?> next) {
      this.factory = factory;
      this.predicate = predicate;
      this.function = function;
      this.next = next;
      isDone = false;
      doneWithOwn = false;
      currentProducer = null;
      currentResult = () -> input;
      updateToNextProducer(input);
    }

    @Override
    public <ResourcePoolT extends ResourcePool> void getNextProtocols(
        ProtocolCollection<ResourcePoolT> protocolCollection) {
      currentProducer.getNextProtocols(protocolCollection);
    }

    private void next() {
      while (!isDone && !currentProducer.hasNextProtocols()) {
        updateToNextProducer(out());
      }
    }

    private void updateToNextProducer(InputT input) {
      if (doneWithOwn) {
        isDone = true;
      } else {
        if (predicate.test(input)) {
          BuilderT builder = factory.createSequential();
          currentResult = function.buildComputation(builder, input);
          currentProducer = builder.build();
        } else {
          doneWithOwn = true;
          if (next != null) {
            currentProducer = next.createProducer(input, factory);
            next = null;
          }
        }
      }
    }

    @Override
    public boolean hasNextProtocols() {
      next();
      return !isDone;
    }

    @Override
    public InputT out() {
      return currentResult.out();
    }
  }
}
