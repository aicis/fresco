package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdzOpenCommitProtocol extends SpdzNativeProtocol<Map<Integer, BigIntegerI>> {

  private SpdzCommitment commitment;
  private Map<Integer, BigIntegerI> ss;
  private Map<Integer, BigIntegerI> commitments;
  private byte[] digest;

  /**
   * Protocol which opens a number of commitments and checks the validity of those.
   *
   * @param commitment My own commitment.
   * @param commitments Other parties commitments.
   */
  public SpdzOpenCommitProtocol(SpdzCommitment commitment,
      Map<Integer, BigIntegerI> commitments) {
    this.commitment = commitment;
    this.commitments = commitments;
    this.ss = new HashMap<>();
  }

  @Override
  public Map<Integer, BigIntegerI> out() {
    return ss;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    int players = spdzResourcePool.getNoOfParties();
    ByteSerializer<BigIntegerI> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      // Send your opening to all players
      BigIntegerI value = this.commitment.getValue();
      network.sendToAll(serializer.serialize(value));
      BigIntegerI randomness = this.commitment.getRandomness();
      network.sendToAll(serializer.serialize(randomness));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      // Receive openings from all parties and check they are valid
      List<byte[]> values = network.receiveFromAll();
      List<byte[]> randomnesses = network.receiveFromAll();

      boolean openingValidated = true;
      BigIntegerI[] broadcastMessages = new BigIntegerI[2 * players];
      for (int i = 0; i < players; i++) {
        BigIntegerI commitment = commitments.get(i + 1);
        BigIntegerI open0 = serializer.deserialize(values.get(i));
        BigIntegerI open1 = serializer.deserialize(randomnesses.get(i));
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
        digest = sendBroadcastValidation(
            spdzResourcePool.getMessageDigest(),
            network, Arrays.asList(broadcastMessages));
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

  private boolean checkCommitment(SpdzResourcePool resourcePool, BigIntegerI commitment,
      BigIntegerI value, BigIntegerI randomness) {
    MessageDigest messageDigest = resourcePool.getMessageDigest();
    messageDigest.update(value.toByteArray());
    messageDigest.update(randomness.toByteArray());
    BigIntegerI testSubject =
        resourcePool.getSerializer().deserialize(messageDigest.digest());
    testSubject.mod(resourcePool.getModulus());
    return commitment.equals(testSubject);
  }
}
