package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
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

  private ProtocolBuilder(boolean parallel,
      ProtocolFactory factory) {
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

  public static <SIntT extends SInt> ProtocolBuilder<SIntT> createParallel(
      BasicNumericFactory<SIntT> factory) {
    return new ProtocolBuilder<>(true, factory);
  }

  public static <SIntT extends SInt> ProtocolBuilder<SIntT> createSequential(
      ProtocolFactory factory) {
    return new ProtocolBuilder<>(false, factory);
  }

  /**
   * Re-creates this basicNumericFactory based on a parallel protocol producer inserted into the
   * original protocol producer.
   *
   * @return the newly created basicNumericFactory
   */
  public ProtocolBuilder<SIntT> createParallelSubFactory() {
    ProtocolEntity<SIntT> protocolEntity = createAndAppend();
    protocolEntity.child = new ProtocolBuilder<>(true, basicNumericFactory);
    return protocolEntity.child;
  }

  /**
   * Re-creates this basicNumericFactory based on a sequential protocol producer inserted into the
   * original protocol producer.
   *
   * @return the newly created basicNumericFactory
   */
  public ProtocolBuilder<SIntT> createSequentialSubFactory() {
    ProtocolEntity<SIntT> protocolEntity = createAndAppend();
    protocolEntity.child = new ProtocolBuilder<>(false, basicNumericFactory);
    return protocolEntity.child;
  }

  private ProtocolEntity<SIntT> createAndAppend() {
    ProtocolEntity<SIntT> protocolEntity = new ProtocolEntity<>();
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

  private void addEntities(ProtocolProducerCollection sequentialProtocolProducer) {
    for (ProtocolEntity protocol : protocols) {
      if (protocol.computation != null) {
        sequentialProtocolProducer.append(protocol.computation);
      } else if (protocol.protocolProducer != null) {
        sequentialProtocolProducer.append(protocol.protocolProducer);
      } else {
        sequentialProtocolProducer.append(protocol.child.build());
      }
    }
  }

  public BasicNumericFactory<SIntT> createAppendingBasicNumericFactory() {
    return new AppendingBasicNumericFactory<>(this.basicNumericFactory, this);
  }

  public ComparisonProtocolFactory createAppendingComparisonProtocolFactory() {
    return new AppendingComparisonProtocolFactory(this.comparisonProtocolFactory, this);
  }

  private static class ProtocolEntity<SIntT extends SInt> {

    Computation<?> computation;
    ProtocolProducer protocolProducer;
    ProtocolBuilder<SIntT> child;
  }
}
