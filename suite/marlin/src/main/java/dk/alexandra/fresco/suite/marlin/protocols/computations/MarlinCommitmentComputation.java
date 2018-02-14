package dk.alexandra.fresco.suite.marlin.protocols.computations;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinAllBroadcastProtocol;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.List;

public class MarlinCommitmentComputation<T extends BigUInt<T>> implements
    ComputationParallel<List<T>, ProtocolBuilderNumeric> {

  private final MarlinResourcePool<T> resourcePool;
  private final T value;

  public MarlinCommitmentComputation(
      MarlinResourcePool<T> resourcePool, T value) {
    // think about logistics of exposing resource pool
    this.resourcePool = resourcePool;
    this.value = value;
  }

  @Override
  public DRes<List<T>> buildComputation(ProtocolBuilderNumeric builder) {
    HashBasedCommitment ownCommitment = new HashBasedCommitment();
    ByteSerializer<HashBasedCommitment> commitmentSerializer = resourcePool
        .getCommitmentSerializer();
    ByteSerializer<T> serializer = resourcePool.getRawSerializer();
    byte[] ownOpening = ownCommitment.commit(
        new AesCtrDrbg(),
        resourcePool.getRawSerializer().serialize(value));
    return builder.seq(new MarlinBroadcastComputation<>(
        commitmentSerializer.serialize(ownCommitment)
    )).seq((seq, rawCommitments) -> {
      DRes<List<byte[]>> openingsDRes = seq.append(new MarlinAllBroadcastProtocol<>(ownOpening));
      List<HashBasedCommitment> commitments = commitmentSerializer.deserializeList(rawCommitments);
      return () -> serializer.deserializeList(open(commitments, openingsDRes.out()));
    });
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
