package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import java.util.function.Supplier;

class LazyProtocolProducer implements ProtocolProducer {

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
