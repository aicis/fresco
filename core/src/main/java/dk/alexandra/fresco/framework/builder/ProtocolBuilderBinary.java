package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ProtocolBuilderBinary implements ProtocolBuilder {

  public AbstractBinaryFactory factory;
  private List<ProtocolEntity> protocols;

  private ProtocolBuilderBinary(AbstractBinaryFactory factory) {
    this.factory = factory;
    this.protocols = new LinkedList<>();
  }

  public static SequentialBinaryBuilder createApplicationRoot(
      AbstractBinaryFactory factory) {
    return new SequentialBinaryBuilder(factory);
  }


  <T extends ProtocolBuilderBinary> void addConsumer(Consumer<T> consumer,
      Supplier<T> supplier) {
    createAndAppend(
        new LazyProtocolProducerDecorator(() -> {
          T builder = supplier.get();
          consumer.accept(builder);
          return builder.build();
        }));
  }

  ProtocolEntity createAndAppend(ProtocolProducer producer) {
    ProtocolEntity protocolEntity = new ProtocolEntity(producer);
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
    createAndAppend(new SingleProtocolProducer(nativeProtocol));
    return nativeProtocol;
  }

  // This will go away and should not be used - users should recode their applications to
  // use closures
  @Override
  @Deprecated
  public <T extends ProtocolProducer> T append(T protocolProducer) {
    createAndAppend(protocolProducer);
    return protocolProducer;
  }

  void addEntities(ProtocolProducerCollection producerCollection) {
    for (ProtocolEntity protocolEntity : protocols) {
      producerCollection.append(protocolEntity.protocolProducer);
    }
  }

  private static class ProtocolEntity {

    final ProtocolProducer protocolProducer;

    private ProtocolEntity(ProtocolProducer producer) {
      protocolProducer = producer;
    }
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
