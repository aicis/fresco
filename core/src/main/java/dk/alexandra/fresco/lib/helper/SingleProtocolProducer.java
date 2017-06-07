package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * A protocol producer that only produces a single protocol.
 */
public class SingleProtocolProducer implements ProtocolProducer {

  private Protocol protocol;
  private boolean evaluated = false;

  private SingleProtocolProducer(Protocol protocol) {
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
   * Creates a new Protocol producer that only serves the protocol in this call
   *
   * @param protocol the protocol to wrap
   * @return the producer
   */
  public static ProtocolProducer wrap(Protocol protocol) {
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
