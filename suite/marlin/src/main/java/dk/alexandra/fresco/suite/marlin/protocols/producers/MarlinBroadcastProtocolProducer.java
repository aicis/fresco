package dk.alexandra.fresco.suite.marlin.protocols.producers;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinAllBroadcastProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinBroadcastValidationProtocol;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MarlinBroadcastProtocolProducer<T extends BigUInt<T>> implements ProtocolProducer {

  private final SequentialProtocolProducer protocolProducer;
  private List<MarlinAllBroadcastProtocol<T>> allBroadcasts;
  private List<byte[]> out;

  MarlinBroadcastProtocolProducer(List<byte[]> input) {
    allBroadcasts = new LinkedList<>();
    protocolProducer = new SequentialProtocolProducer();
    protocolProducer.lazyAppend(() -> {
      ParallelProtocolProducer parallel = new ParallelProtocolProducer();
      for (byte[] singleInput : input) {
        MarlinAllBroadcastProtocol<T> broadcast = new MarlinAllBroadcastProtocol<>(singleInput);
        allBroadcasts.add(broadcast);
        parallel.append(new SingleProtocolProducer<>(broadcast));
      }
      return parallel;
    });
    protocolProducer.lazyAppend(
        () -> {
          out = allBroadcasts.stream()
              .flatMap(broadcast -> broadcast.out().stream())
              .collect(Collectors.toList());
          return new SingleProtocolProducer<>(
              new MarlinBroadcastValidationProtocol<>(out));
        }
    );
  }

  MarlinBroadcastProtocolProducer(byte[] input) {
    this(Collections.singletonList(input));
  }

  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    protocolProducer.getNextProtocols(protocolCollection);
  }

  @Override
  public boolean hasNextProtocols() {
    return protocolProducer.hasNextProtocols();
  }

  public List<byte[]> out() {
    return out;
  }

}
