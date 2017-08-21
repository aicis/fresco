package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import java.util.function.Predicate;

class BuildStepLooping<InputT> extends
    BuildStep<SequentialNumericBuilder, InputT, InputT> {

  private final Predicate<InputT> predicate;
  private final FrescoLambda<InputT, InputT> function;

  BuildStepLooping(
      Predicate<InputT> predicate,
      FrescoLambda<InputT, InputT> function) {
    super(function);
    this.predicate = predicate;
    this.function = function;
  }

  @Override
  protected ProtocolProducer createProducer(InputT input, BuilderFactoryNumeric factory) {
    LoopProtocolProducer<InputT> loopProtocolProducer = new LoopProtocolProducer<>(factory, input,
        predicate, function, next);
    output = loopProtocolProducer;
    return loopProtocolProducer;
  }

  @Override
  protected SequentialNumericBuilder createBuilder(
      BuilderFactoryNumeric factory) {
    throw new IllegalStateException("Should not be called");
  }

  private static class LoopProtocolProducer<InputT> implements ProtocolProducer,
      Computation<InputT> {

    private final BuilderFactoryNumeric factory;
    private boolean isDone;
    private boolean doneWithOwn;
    private Computation<InputT> currentResult;
    private ProtocolProducer currentProducer;
    private Predicate<InputT> predicate;
    private FrescoLambda<InputT, InputT> function;
    private BuildStep<?, ?, InputT> next;

    LoopProtocolProducer(BuilderFactoryNumeric factory,
        InputT input,
        Predicate<InputT> predicate,
        FrescoLambda<InputT, InputT> function,
        BuildStep<?, ?, InputT> next) {
      this.factory = factory;
      this.predicate = predicate;
      this.function = function;
      this.next = next;
      isDone = false;
      doneWithOwn = false;
      currentProducer = null;
      updateToNextProducer(input);
    }

    @Override
    public void getNextProtocols(ProtocolCollection protocolCollection) {
      currentProducer.getNextProtocols(protocolCollection);
    }

    private void next() {
      while (!isDone && !currentProducer.hasNextProtocols()) {
        updateToNextProducer(currentResult.out());
      }
    }

    private void updateToNextProducer(InputT input) {
      if (doneWithOwn) {
        isDone = true;
      } else {
        if (predicate.test(input)) {
          SequentialNumericBuilder builder = new SequentialNumericBuilder(factory);
          currentResult = function.apply(input, builder);
          currentProducer = builder.build();
        } else {
          doneWithOwn = true;
          currentResult = () -> input;
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
