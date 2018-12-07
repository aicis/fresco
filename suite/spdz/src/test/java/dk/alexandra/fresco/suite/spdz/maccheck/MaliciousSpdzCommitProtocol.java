package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MaliciousSpdzCommitProtocol extends SpdzNativeProtocol<Boolean> {

  private SpdzCommitment commitment;
  private Map<Integer, FieldElement> comms;
  private byte[] broadcastDigest;
  private Boolean result;
  private final boolean corruptNow;

  /**
   * Malicious commitment protocol.
   */
  public MaliciousSpdzCommitProtocol(SpdzCommitment commitment, Map<Integer, FieldElement> comms,
      boolean corruptNow) {
    this.commitment = commitment;
    this.comms = comms;
    this.corruptNow = corruptNow;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool, Network network) {
    int players = spdzResourcePool.getNoOfParties();
    ByteSerializer<FieldElement> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      network.sendToAll(
          serializer.serialize(commitment.computeCommitment(spdzResourcePool.getSerializer())));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {

      List<byte[]> commitments = network.receiveFromAll();
      for (int i = 0; i < commitments.size(); i++) {
        comms.put(i + 1, serializer.deserialize(commitments.get(i)));
      }
      if (players < 3) {
        this.result = true;
        return EvaluationStatus.IS_DONE;
      } else {
        broadcastDigest = sendMaliciousBroadcastValidation(spdzResourcePool.getMessageDigest(),
            network, comms.values());
        return EvaluationStatus.HAS_MORE_ROUNDS;
      }
    } else {
      this.result = receiveMaliciousBroadcastValidation(network, broadcastDigest);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public Boolean out() {
    return result;
  }

  private byte[] sendMaliciousBroadcastValidation(MessageDigest dig, Network network,
      Collection<FieldElement> bs) {
    for (FieldElement b : bs) {
      dig.update(b.convertValueToBigInteger().toByteArray());
    }
    return sendAndReset(dig, network);
  }

  private byte[] sendAndReset(MessageDigest dig, Network network) {
    byte[] digest = dig.digest();
    dig.reset();
    if (corruptNow) {
      digest[0] = (byte) 0xFF;
    }
    network.sendToAll(digest);
    return digest;
  }

  private boolean receiveMaliciousBroadcastValidation(Network network, byte[] digest) {
    // TODO: should we check that we get messages from all players?
    boolean validated = true;
    List<byte[]> digests = network.receiveFromAll();
    for (byte[] d : digests) {
      boolean equals = Arrays.equals(d, digest);
      validated = validated && equals;
    }
    return validated;
  }
}
