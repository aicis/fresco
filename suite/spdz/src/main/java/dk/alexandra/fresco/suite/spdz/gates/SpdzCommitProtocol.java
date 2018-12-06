package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdzCommitProtocol extends SpdzNativeProtocol<Map<Integer, FieldElement>> {

  private SpdzCommitment commitment;
  private Map<Integer, FieldElement> comms;
  private byte[] broadcastDigest;

  public SpdzCommitProtocol(SpdzCommitment commitment) {
    this.commitment = commitment;
    this.comms = new HashMap<>();
  }

  @Override
  public Map<Integer, FieldElement> out() {
    return comms;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    int players = spdzResourcePool.getNoOfParties();
    ByteSerializer<FieldElement> serializer = spdzResourcePool.getSerializer();

    if (round == 0) {
      network.sendToAll(serializer.serialize(commitment.computeCommitment(serializer)));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {

      List<byte[]> commitments = network.receiveFromAll();
      for (int i = 0; i < commitments.size(); i++) {
        comms.put(i + 1, serializer.deserialize(commitments.get(i)));
      }
      if (players < 3) {
        return EvaluationStatus.IS_DONE;
      } else {
        broadcastDigest = sendBroadcastValidation(
            serializer,
            spdzResourcePool.getMessageDigest(), network, comms.values()
        );
        return EvaluationStatus.HAS_MORE_ROUNDS;
      }
    } else {
      if (!receiveBroadcastValidation(network, broadcastDigest)) {
        throw new MaliciousException(
            "Malicious activity detected: Broadcast of commitments was not validated.");
      }
      return EvaluationStatus.IS_DONE;
    }
  }
}
