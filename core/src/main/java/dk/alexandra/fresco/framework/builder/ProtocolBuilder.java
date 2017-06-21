package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.RightShiftBuilder;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Central class that allowes building complex trees of protocol producers based on
 * the sequence in which they are created. This class stores the intention of building
 * a protocol producer rather than the actual protocol producer and only when requested
 * actually evaluates the closure and returns the actual protocol producer.
 */
public abstract class ProtocolBuilder<SIntT extends SInt> {

  private BasicNumericFactory<SIntT> basicNumericFactory;
  private List<ProtocolEntity> protocols;
  private BuilderFactoryNumeric<SIntT> factory;
  private NumericBuilder<SIntT> numericBuilder;

  private ProtocolBuilder(BuilderFactoryNumeric<SIntT> factory) {
    this.factory = factory;
    this.basicNumericFactory = factory.getBasicNumericFactory();
    this.protocols = new LinkedList<>();
  }

  public OIntFactory getOIntFactory() {
    return basicNumericFactory;
  }

  public SIntFactory getSIntFactory() {
    return basicNumericFactory;
  }

  public static <SIntT extends SInt> SequentialProtocolBuilder<SIntT> createRoot(
      BuilderFactoryNumeric<SIntT> factory, Consumer<SequentialProtocolBuilder<SIntT>> consumer) {
    SequentialProtocolBuilder<SIntT> builder = new SequentialProtocolBuilder<>(factory);
    builder.addConsumer(consumer, () -> new SequentialProtocolBuilder<>(factory));
    return builder;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested parallel protocol
   * producer inserted into the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <R, T extends Function<ParallelProtocolBuilder<SIntT>, Computation<R>>>
  Computation<R> createParallelSubFactoryReturning(T function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.apply(builder)),
        () -> new ParallelProtocolBuilder<>(factory));
    return result;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested sequential protocol
   * producer inserted into the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <R, T extends Function<SequentialProtocolBuilder<SIntT>, Computation<R>>>
  Computation<R> createSequentialSubFactoryReturning(T function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.apply(builder)),
        () -> new SequentialProtocolBuilder<>(factory));
    return result;
  }

  /**
   * Re-creates this basicNumericFactory based on a sequential protocol producer inserted into the
   * original protocol producer.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<SequentialProtocolBuilder<SIntT>>>
  T createSequentialSubFactory(T consumer) {
    addConsumer(consumer, () -> new SequentialProtocolBuilder<>(factory));
    return consumer;
  }

  <T extends ProtocolBuilder<SIntT>> void addConsumer(Consumer<T> consumer,
      Supplier<T> supplier) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.child = new LazyProtocolProducer(() -> {
      T builder = supplier.get();
      consumer.accept(builder);
      return builder.build();
    });
  }

  private ProtocolEntity createAndAppend() {
    ProtocolEntity protocolEntity = new ProtocolEntity();
    protocols.add(protocolEntity);
    return protocolEntity;
  }

  public <T extends NativeProtocol> T append(T computation) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = SingleProtocolProducer.wrap(computation);
    return computation;
  }

  public <T extends ProtocolProducer> T append(T protocolProducer) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = protocolProducer;
    return protocolProducer;
  }

  public abstract ProtocolProducer build();

  void addEntities(ProtocolProducerCollection producerCollection) {
    for (ProtocolEntity protocolEntity : protocols) {
      if (protocolEntity.computation != null) {
        producerCollection.append(protocolEntity.computation);
      } else if (protocolEntity.protocolProducer != null) {
        producerCollection.append(protocolEntity.protocolProducer);
      } else {
        producerCollection.append(protocolEntity.child);
      }
    }
  }

  public NumericBuilder<SIntT> numeric() {
    if (numericBuilder == null) {
      numericBuilder = factory.createNumericBuilder(this);
    }
    return numericBuilder;
  }

  public ComparisonBuilder<SIntT> createComparisonBuilder() {
    return factory.createComparisonBuilder(this);
  }

  public InnerProductBuilder<SIntT> createInnerProductBuilder() {
    return factory.createInnerProductBuilder(this);
  }

  public RandomAdditiveMaskBuilder<SIntT> createAdditiveMaskBuilder() {
    return factory.createAdditiveMaskBuilder(this);
  }

  public OpenBuilder<SIntT> createOpenBuilder() {
    return factory.createOpenBuilder(this);
  }

  public RightShiftBuilder<SIntT> createRightShiftBuilder() {
    return factory.createRightShiftBuilder(this);
  }


  private static class ProtocolEntity {

    Computation<?> computation;
    ProtocolProducer protocolProducer;
    LazyProtocolProducer child;
  }

  private static class LazyProtocolProducer implements ProtocolProducer {

    private ProtocolProducer protocolProducer;
    private Supplier<ProtocolProducer> child;

    LazyProtocolProducer(Supplier<ProtocolProducer> supplier) {
      this.child = supplier;
    }

    @Override
    public void getNextProtocols(ProtocolCollection protocolCollection) {
      checkReady();
      protocolProducer.getNextProtocols(protocolCollection);
    }

    @Override
    public boolean hasNextProtocols() {
      checkReady();
      return protocolProducer.hasNextProtocols();
    }

    private void checkReady() {
      if (protocolProducer == null) {
        protocolProducer = child.get();
        child = null;
      }
    }
  }

  public static class SequentialProtocolBuilder<SIntT extends SInt> extends ProtocolBuilder<SIntT> {

    private SequentialProtocolBuilder(BuilderFactoryNumeric<SIntT> factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      SequentialProtocolProducer parallelProtocolProducer = new SequentialProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    }
  }

  public static class ParallelProtocolBuilder<SIntT extends SInt> extends ProtocolBuilder<SIntT> {

    private ParallelProtocolBuilder(BuilderFactoryNumeric<SIntT> factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      SequentialProtocolProducer parallelProtocolProducer = new SequentialProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    }
  }
}
