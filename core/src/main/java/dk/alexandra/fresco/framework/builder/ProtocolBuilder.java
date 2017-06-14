package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
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
public class ProtocolBuilder<SIntT extends SInt> {

  private static final int MAGIC_SECURE_NUMBER = 60;
  private boolean parallel;
  private BasicNumericFactory<SIntT> basicNumericFactory;
  private ComparisonProtocolFactory comparisonProtocolFactory;
  private List<ProtocolEntity> protocols;

  private LocalInversionFactory localInvFactory;
  private NumericBitFactory numericBitFactory;
  private ExpFromOIntFactory expFromOIntFactory;
  private PreprocessedExpPipeFactory expFactory;

  private ProtocolBuilder(boolean parallel, ProtocolFactory factory) {
    this.parallel = parallel;
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

  public static <SIntT extends SInt> ProtocolBuilder<SIntT> createSequential(
      ProtocolFactory factory, Consumer<ProtocolBuilder<SIntT>> consumer) {
    ProtocolBuilder<SIntT> builder = new ProtocolBuilder<>(false, factory);
    builder.addConsumer(consumer, false);
    return builder;
  }

  /**
   * Re-creates this basicNumericFactory based on a parallel protocol producer inserted into the
   * original protocol producer.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public void createParallelSubFactory(Consumer<ProtocolBuilder<SIntT>> consumer) {
    addConsumer(consumer, true);
  }

  /**
   * Re-creates this basicNumericFactory based on a sequential protocol producer inserted into the
   * original protocol producer.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public void createSequentialSubFactory(Consumer<ProtocolBuilder<SIntT>> consumer) {
    addConsumer(consumer, false);
  }

  private void addConsumer(Consumer<ProtocolBuilder<SIntT>> consumer, boolean parallel) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.child = new LazyProtocolProducer(() -> {
      ProtocolBuilder<SIntT> builder = new ProtocolBuilder<>(parallel, basicNumericFactory);
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
    protocolEntity.computation = computation;
  }

  public void append(ProtocolProducer protocolProducer) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = protocolProducer;
  }

  public ProtocolProducer build() {
    if (parallel) {
      ParallelProtocolProducer parallelProtocolProducer = new ParallelProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    } else {
      SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
      addEntities(sequentialProtocolProducer);
      return sequentialProtocolProducer;
    }
  }

  private void addEntities(ProtocolProducerCollection producerCollection) {
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

  private static class LazyProtocolProducer<SIntT extends SInt> implements ProtocolProducer {

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
}
