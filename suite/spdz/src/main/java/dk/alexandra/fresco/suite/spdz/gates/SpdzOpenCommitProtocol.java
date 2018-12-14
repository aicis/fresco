package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdzOpenCommitProtocol extends SpdzNativeProtocol<Map<Integer, FieldElement>> {

  private SpdzCommitment commitment;
  private Map<Integer, FieldElement> ss;
  private Map<Integer, byte[]> commitments;
  private byte[] digest;

  /**
   * Protocol which opens a number of commitments and checks the validity of those.
   *
   * @param commitment My own commitment.
   * @param commitments Other parties commitments.
   */
  public SpdzOpenCommitProtocol(SpdzCommitment commitment,
      Map<Integer, byte[]> commitments) {
    this.commitment = commitment;
    this.commitments = commitments;
    this.ss = new HashMap<>();
  }

  @Override
  public Map<Integer, FieldElement> out() {
    return ss;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    int players = spdzResourcePool.getNoOfParties();
    FieldDefinition definition = spdzResourcePool.getFieldDefinition();
    if (round == 0) {
      // Send your opening to all players
      FieldElement value = this.commitment.getValue();
      network.sendToAll(definition.serialize(value));
      FieldElement randomness = this.commitment.getRandomness();
      network.sendToAll(definition.serialize(randomness));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      // Receive openings from all parties and check they are valid
      List<byte[]> values = network.receiveFromAll();
      List<byte[]> randomnesses = network.receiveFromAll();

      boolean openingValidated = true;
      byte[][] broadcastMessages = new byte[2 * players][];
      for (int i = 0; i < players; i++) {
        byte[] commitment = commitments.get(i + 1);
        byte[] open0 = values.get(i);
        byte[] open1 = randomnesses.get(i);
        boolean validate = checkCommitment(
            spdzResourcePool, commitment, open0, open1);
        openingValidated = openingValidated && validate;
        ss.put(i, spdzResourcePool.getFieldDefinition().deserialize(open0));
        broadcastMessages[i * 2] = open0;
        broadcastMessages[i * 2 + 1] = open1;
      }
      if (players < 3) {
        checkValidation(openingValidated);
        return EvaluationStatus.IS_DONE;
      } else {
        digest = sendBroadcastValidation(
            spdzResourcePool.getMessageDigest(), network, Arrays.asList(broadcastMessages));
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      // If more than three players check if openings where
      // broadcasted correctly
      checkValidation(receiveBroadcastValidation(network, digest));
      return EvaluationStatus.IS_DONE;
    }
  }

  public void checkValidation(boolean valid) {
    if (!valid) {
      throw new MaliciousException("Malicious activity detected: Opening commitments failed.");
    }
  }

  private boolean checkCommitment(SpdzResourcePool resourcePool, byte[] commitment,
      byte[] value, byte[] randomness) {
    MessageDigest messageDigest = resourcePool.getMessageDigest();
    messageDigest.update(value);
    messageDigest.update(randomness);
    byte[] testSubject = messageDigest.digest();
    return Arrays.equals(commitment, testSubject);
  }
}
