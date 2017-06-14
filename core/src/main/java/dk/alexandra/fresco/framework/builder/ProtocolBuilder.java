package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Central class that allowes building complex trees of protocol producers based on
 * the sequence in which they are created.
 */
public abstract class ProtocolBuilder<SIntT extends SInt> {

  private static final int MAGIC_SECURE_NUMBER = 60;
  private BasicNumericFactory<SIntT> basicNumericFactory;
  private ComparisonProtocolFactory comparisonProtocolFactory;
  private List<ProtocolEntity> protocols;

  private LocalInversionFactory localInvFactory;
  private NumericBitFactory numericBitFactory;
  private ExpFromOIntFactory expFromOIntFactory;
  private PreprocessedExpPipeFactory expFactory;

  private ProtocolBuilder(ProtocolFactory factory) {
    if (factory instanceof BasicNumericFactory) {
      this.basicNumericFactory = (BasicNumericFactory<SIntT>) factory;
    }
    if (factory instanceof LocalInversionFactory) {
      localInvFactory = (LocalInversionFactory) factory;
    }
    if (factory instanceof NumericBitFactory) {
      numericBitFactory = (NumericBitFactory) factory;
    }
    if (factory instanceof ExpFromOIntFactory) {
      expFromOIntFactory = (ExpFromOIntFactory) factory;
    }
    if (factory instanceof PreprocessedExpPipeFactory) {
      expFactory = (PreprocessedExpPipeFactory) factory;
    }

    if (basicNumericFactory != null
        && localInvFactory != null
        && numericBitFactory != null
        && expFromOIntFactory != null
        && expFactory != null) {
      this.comparisonProtocolFactory =
          new ComparisonProtocolFactoryImpl(MAGIC_SECURE_NUMBER, basicNumericFactory,
              localInvFactory, numericBitFactory, expFromOIntFactory, expFactory);
    }
    this.protocols = new LinkedList<>();
  }

  public static <SIntT extends SInt> ProtocolBuilder<SIntT> createRoot(
      ProtocolFactory factory, Consumer<SequentialProtocolBuilder<SIntT>> consumer) {
    ProtocolBuilder<SIntT> builder = new SequentialProtocolBuilder<>(factory);
    builder.addConsumer(consumer, () -> new SequentialProtocolBuilder<>(factory));
    return builder;
  }

  /**
   * Re-creates this basicNumericFactory based on a parallel protocol producer inserted into the
   * original protocol producer.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<ParallelProtocolBuilder<SIntT>>>
  T createParallelSubFactory(T consumer) {
    addConsumer(consumer, () -> new ParallelProtocolBuilder<>(basicNumericFactory));
    return consumer;
  }

  /**
   * Re-creates this basicNumericFactory based on a sequential protocol producer inserted into the
   * original protocol producer.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<SequentialProtocolBuilder<SIntT>>>
  T createSequentialSubFactory(T consumer) {
    addConsumer(consumer, () -> new SequentialProtocolBuilder<>(basicNumericFactory));
    return consumer;
  }

  private <T extends ProtocolBuilder<SIntT>> void addConsumer(Consumer<T> consumer,
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

  public void append(Computation<? extends SInt> computation) {
    ProtocolEntity protocolEntity = createAndAppend();
    if (computation instanceof NativeProtocol) {
      protocolEntity.protocolProducer = SingleProtocolProducer.wrap(computation);
    } else if (computation instanceof ProtocolProducer) {
      protocolEntity.protocolProducer = (ProtocolProducer) computation;
    } else {
      throw new IllegalArgumentException("Cannot append " + computation
          + " - must either be a protocol producer or a native protocol");
    }
  }

  public void append(ProtocolProducer protocolProducer) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = protocolProducer;
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

  public BasicNumericFactory<SIntT> createAppendingBasicNumericFactory() {
    return new AppendingBasicNumericFactory<>(this.basicNumericFactory, this);
  }

  public ComparisonProtocolFactory createAppendingComparisonProtocolFactory() {
    return new AppendingComparisonProtocolFactory(this.comparisonProtocolFactory, this);
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

    private SequentialProtocolBuilder(ProtocolFactory factory) {
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

    private ParallelProtocolBuilder(ProtocolFactory factory) {
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
