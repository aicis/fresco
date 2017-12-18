package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class SpdzCommitProtocol extends SpdzNativeProtocol<Void> {

  private SpdzCommitment commitment;
  private Map<Integer, BigInteger> comms;
  private byte[] broadcastDigest;

  public SpdzCommitProtocol(SpdzCommitment commitment,
      Map<Integer, BigInteger> comms) {
    this.commitment = commitment;
    this.comms = comms;
  }

  @Override
  public Void out() {
    return null;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    int players = spdzResourcePool.getNoOfParties();
    BigIntegerSerializer serializer = spdzResourcePool.getSerializer();
    if(round == 0) {
      network.sendToAll(serializer
          .toBytes(commitment.computeCommitment(spdzResourcePool.getModulus())));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {

      List<byte[]> commitments = network.receiveFromAll();
      for (int i = 0; i < commitments.size(); i++) {
        comms.put(i + 1, serializer.toBigInteger(commitments.get(i)));
      }
      if (players < 3) {
        return EvaluationStatus.IS_DONE;
      } else {
        broadcastDigest = sendBroadcastValidation(
            spdzResourcePool.getMessageDigest(), network, comms.values()
            );
        return EvaluationStatus.HAS_MORE_ROUNDS;
      }
    } else {
      boolean validated = receiveBroadcastValidation(network, broadcastDigest);
      if (!validated) {
        throw new MPCException(
            "Broadcast of commitments was not validated. Abort protocol.");
      }
      return EvaluationStatus.IS_DONE;
    }
  }
}
