package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MaliciousSpdzOpenCommitProtocol extends SpdzNativeProtocol<Boolean> {

  private SpdzCommitment commitment;
  private Map<Integer, FieldElement> ss;
  private Map<Integer, FieldElement> commitments;
  private boolean openingValidated;
  private byte[] digest;
  private Boolean result;

  private final boolean corruptNow;

  /**
   * Malicious Protocol which opens a number of commitments and checks the validity of those.
   *
   * @param commitment My own commitment.
   * @param commitments Other parties commitments.
   * @param ss The resulting opened values from the commitments.
   */
  public MaliciousSpdzOpenCommitProtocol(SpdzCommitment commitment,
      Map<Integer, FieldElement> commitments, Map<Integer, FieldElement> ss, boolean corruptNow) {
    this.commitment = commitment;
    this.commitments = commitments;
    this.ss = ss;
    this.corruptNow = corruptNow;
  }

  @Override
  public Boolean out() {
    return result;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool, Network network) {
    int players = spdzResourcePool.getNoOfParties();
    FieldDefinition definition = spdzResourcePool.getFieldDefinition();
    if (round == 0) {
      // Send your opening to all players
      FieldElement value = this.commitment.getValue();
      network.sendToAll(definition.serialize(value));
      FieldElement randomness = this.commitment.getRandomness();
      if (corruptNow) {
        randomness = randomness.add(definition.createElement(1));
      }
      network.sendToAll(definition.serialize(randomness));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      // Receive openings from all parties and check they are valid
      List<byte[]> values = network.receiveFromAll();
      List<byte[]> randomnesses = network.receiveFromAll();

      openingValidated = true;
      FieldElement[] broadcastMessages = new FieldElement[2 * players];
      for (int i = 0; i < players; i++) {
        FieldElement com = commitments.get(i + 1);
        FieldElement open0 = definition.deserialize(values.get(i));
        FieldElement open1 = definition.deserialize(randomnesses.get(i));
        boolean validate = checkCommitment(spdzResourcePool, com, open0, open1);
        openingValidated = openingValidated && validate;
        ss.put(i, open0);
        broadcastMessages[i * 2] = open0;
        broadcastMessages[i * 2 + 1] = open1;
      }
      if (players < 3) {
        this.result = openingValidated;
        return EvaluationStatus.IS_DONE;
      } else {
        digest = sendMaliciousBroadcastValidation(spdzResourcePool.getMessageDigest(), network,
            Arrays.asList(broadcastMessages));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      // If more than three players check if openings where
      // broadcasted correctly
      this.result = receiveMaliciousBroadcastValidation(network, digest);
      return EvaluationStatus.IS_DONE;
    }
  }

  private boolean checkCommitment(SpdzResourcePool spdzResourcePool, FieldElement commitment,
      FieldElement value, FieldElement randomness) {
    FieldDefinition definition = spdzResourcePool.getFieldDefinition();
    MessageDigest messageDigest = spdzResourcePool.getMessageDigest();
    messageDigest.update(definition.serialize(value));
    messageDigest.update(definition.serialize(randomness));
    FieldElement testSubject = definition.deserialize(messageDigest.digest());
    return commitment.equals(testSubject);
  }

  private byte[] sendMaliciousBroadcastValidation(MessageDigest dig, Network network,
      Collection<FieldElement> bs) {
    for (FieldElement b : bs) {
      dig.update(b.convertToBigInteger().toByteArray());
    }
    return sendAndReset(dig, network);
  }

  private byte[] sendAndReset(MessageDigest dig, Network network) {
    byte[] digest = dig.digest();
    dig.reset();
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

