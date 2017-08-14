package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import java.util.function.Predicate;

class BuildStepBinaryLooping<InputT>
    extends BuildStepBinary<SequentialBinaryBuilder, InputT, InputT> {

  private final Predicate<InputT> predicate;
  private final FrescoLambdaBinary<InputT, InputT> function;

  BuildStepBinaryLooping(Predicate<InputT> predicate, FrescoLambdaBinary<InputT, InputT> function) {
    super(function);
    this.predicate = predicate;
    this.function = function;
  }

  @Override
  protected ProtocolProducer createProducer(InputT input, BuilderFactoryBinary factory) {
    LoopProtocolProducer<InputT> loopProtocolProducer =
        new LoopProtocolProducer<>(factory, input, predicate, function, next);
    output = loopProtocolProducer;
    return loopProtocolProducer;
  }

  @Override
  protected SequentialBinaryBuilder createBuilder(BuilderFactoryBinary factory) {
    throw new IllegalStateException("Should not be called");
  }

  private static class LoopProtocolProducer<InputT>
      implements ProtocolProducer, Computation<InputT> {

    private final BuilderFactoryBinary factory;
    private boolean isDone;
    private boolean doneWithOwn;
    private Computation<InputT> currentResult;
    private ProtocolProducer currentProducer;
    private Predicate<InputT> predicate;
    private FrescoLambdaBinary<InputT, InputT> function;
    private BuildStepBinary<?, ?, InputT> next;

    LoopProtocolProducer(BuilderFactoryBinary factory, InputT input, Predicate<InputT> predicate,
        FrescoLambdaBinary<InputT, InputT> function, BuildStepBinary<?, ?, InputT> next) {
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
          SequentialBinaryBuilder builder = new SequentialBinaryBuilder(factory);
          currentResult = function.apply(input, builder);
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
