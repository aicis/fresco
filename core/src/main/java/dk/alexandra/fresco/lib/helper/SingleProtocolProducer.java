package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * A protocol producer that only produces a single protocol.
 */
public class SingleProtocolProducer implements ProtocolProducer {

  private NativeProtocol protocol;
  private boolean evaluated = false;

  private SingleProtocolProducer(NativeProtocol protocol) {
    this.protocol = protocol;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (!protocolCollection.hasFreeCapacity()) {
      return;
    }
    if (!evaluated) {
      evaluated = true;
      protocolCollection.addProtocol(protocol);
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return !evaluated;
  }

  /**
   * Creates a new NativeProtocol producer that only serves the protocol in this call
   *
   * @param protocol the protocol to wrap
   * @return the producer
   */
  public static ProtocolProducer wrap(NativeProtocol protocol) {
    if (protocol == null) {
      return null;
    }
    return new SingleProtocolProducer(protocol);
  }

  @Override
  public String toString() {
    return "SingleProtocolProducer{"
        + "protocol=" + protocol
        + '}';
  }
}
