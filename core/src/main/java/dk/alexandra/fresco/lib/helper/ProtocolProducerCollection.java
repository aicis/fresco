package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * ProtocolProducers that can have other Protocolproducers appended to them.
 *
 */
public interface ProtocolProducerCollection {

  /**
   * Appends a ProtocolProducer to this ProtocolProducer. The exact meaning of appending a
   * ProtocolProducer is dependent is defined by this ProtocolProducer. However, as a minimum
   * calling nextProtocols on this ProtocolProducer should eventually produce the Protocols of the
   * appended ProtocolProducer.
   *
   * @param protocolProducer the protocol producer to append
   */
  void append(ProtocolProducer protocolProducer);

}
