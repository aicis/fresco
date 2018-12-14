package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdzOpenCommitProtocol extends SpdzNativeProtocol<Map<Integer, FieldElement>> {

  private SpdzCommitment commitment;
  private Map<Integer, FieldElement> ss;
  private Map<Integer, FieldElement> commitments;
  private byte[] digest;

  /**
   * Protocol which opens a number of commitments and checks the validity of those.
   *
   * @param commitment My own commitment.
   * @param commitments Other parties commitments.
   */
  public SpdzOpenCommitProtocol(SpdzCommitment commitment,
      Map<Integer, FieldElement> commitments) {
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
      FieldElement[] broadcastMessages = new FieldElement[2 * players];
      for (int i = 0; i < players; i++) {
        FieldElement commitment = commitments.get(i + 1);
        FieldElement open0 = definition.deserialize(values.get(i));
        FieldElement open1 = definition.deserialize(randomnesses.get(i));
        boolean validate = checkCommitment(
            spdzResourcePool, commitment, open0, open1);
        openingValidated = openingValidated && validate;
        ss.put(i, open0);
        broadcastMessages[i * 2] = open0;
        broadcastMessages[i * 2 + 1] = open1;
      }
      if (players < 3) {
        checkValidation(openingValidated);
        return EvaluationStatus.IS_DONE;
      } else {
        digest = sendBroadcastValidation(definition,
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

  private boolean checkCommitment(SpdzResourcePool resourcePool, FieldElement commitment,
      FieldElement value, FieldElement randomness) {
    FieldDefinition definition = resourcePool.getFieldDefinition();
    MessageDigest messageDigest = resourcePool.getMessageDigest();
    messageDigest.update(definition.serialize(value));
    messageDigest.update(definition.serialize(randomness));
    FieldElement testSubject = definition.createElement(new BigInteger(messageDigest.digest()));
    return commitment.equals(testSubject);
  }
}
