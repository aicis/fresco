package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinAllBroadcastProtocol;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.List;

public class MarlinCommitmentProtocolProducer<T extends BigUInt<T>> implements ProtocolProducer {

  private final SequentialProtocolProducer protocolProducer;
  private final byte[] ownOpening;
  private MarlinBroadcastProtocolProducer<T> commitmentBroadcast;
  private MarlinAllBroadcastProtocol<T> openingBroadcast;
  private List<T> result;

  MarlinCommitmentProtocolProducer(MarlinResourcePool<T> resourcePool, T value) {
    ByteSerializer<HashBasedCommitment> commitmentSerializer = resourcePool
        .getCommitmentSerializer();
    final HashBasedCommitment ownCommitment = new HashBasedCommitment();
    ownOpening = ownCommitment.commit(
        new AesCtrDrbg(),
        resourcePool.getRawSerializer().serialize(value));
    protocolProducer = new SequentialProtocolProducer();
    protocolProducer.lazyAppend(() -> {
      byte[] serialized = resourcePool.getCommitmentSerializer().serialize(ownCommitment);
      commitmentBroadcast = new MarlinBroadcastProtocolProducer<>(serialized);
      return commitmentBroadcast;
    });
    protocolProducer.lazyAppend(() -> {
      openingBroadcast = new MarlinAllBroadcastProtocol<>(ownOpening);
      return new SingleProtocolProducer<>(openingBroadcast);
    });
    protocolProducer.lazyAppend(() -> {
      List<HashBasedCommitment> commitments = commitmentSerializer
          .deserializeList(commitmentBroadcast.out());
      List<byte[]> openings = openingBroadcast.out();
      List<byte[]> opened = open(commitments, openings);
      result = resourcePool.getRawSerializer().deserializeList(opened);
      return new SequentialProtocolProducer();
    });
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

  public List<T> getResult() {
    return result;
  }

  private List<byte[]> open(List<HashBasedCommitment> commitments, List<byte[]> openings) {
    if (commitments.size() != openings.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    List<byte[]> result = new ArrayList<>(commitments.size());
    for (int i = 0; i < commitments.size(); i++) {
      HashBasedCommitment commitment = commitments.get(i);
      byte[] opening = openings.get(i);
      result.add(commitment.open(opening));
    }
    return result;
  }

}
