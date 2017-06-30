package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ProtocolBuilderBinary implements ProtocolBuilder {

  AbstractBinaryFactory factory;
  private List<ProtocolEntity> protocols;

  private ProtocolBuilderBinary(AbstractBinaryFactory factory) {
    this.factory = factory;
    this.protocols = new LinkedList<>();
  }

  public static SequentialBinaryBuilder createApplicationRoot(
      AbstractBinaryFactory factory) {
    SequentialBinaryBuilder builder = new SequentialBinaryBuilder(factory);
    return builder;

  }


  <T extends ProtocolBuilderBinary> void addConsumer(Consumer<T> consumer,
      Supplier<T> supplier) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.child = new LazyProtocolProducer(() -> {
      T builder = supplier.get();
      consumer.accept(builder);
      return builder.build();
    });
  }

  ProtocolEntity createAndAppend() {
    ProtocolEntity protocolEntity = new ProtocolEntity();
    protocols.add(protocolEntity);
    return protocolEntity;
  }


  /**
   * Appends a concrete, native protocol to the list of producers - udeful for the native protocol
   * factroies that needs to be builders.
   *
   * @param nativeProtocol the native protocol to add
   * @param <T> the type of the native protocol - passthrough buildable object
   * @return the original native protocol.
   */
  public <T extends NativeProtocol> T append(T nativeProtocol) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = SingleProtocolProducer.wrap(nativeProtocol);
    return nativeProtocol;
  }

  // This will go away and should not be used - users should recode their applications to
  // use closures
  @Override
  @Deprecated
  public <T extends ProtocolProducer> T append(T protocolProducer) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = protocolProducer;
    return protocolProducer;
  }

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

  private static class ProtocolEntity {

    Computation<?> computation;
    ProtocolProducer protocolProducer;
    LazyProtocolProducer child;
  }

  /**
   * A specific instance of the protocol builder that produces a sequential producer.
   */
  public static class SequentialBinaryBuilder extends ProtocolBuilderBinary {

    private SequentialBinaryBuilder(AbstractBinaryFactory factory) {
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
