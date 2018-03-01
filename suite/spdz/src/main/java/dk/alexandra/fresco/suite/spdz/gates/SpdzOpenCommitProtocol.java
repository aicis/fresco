package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdzOpenCommitProtocol extends SpdzNativeProtocol<Map<Integer, BigInteger>> {

  private SpdzCommitment commitment;
  private Map<Integer, BigInteger> ss;
  private Map<Integer, BigInteger> commitments;
  private byte[] digest;

  /**
   * Protocol which opens a number of commitments and checks the validity of those.
   *
   * @param commitment My own commitment.
   * @param commitments Other parties commitments.
   */
  public SpdzOpenCommitProtocol(SpdzCommitment commitment,
      Map<Integer, BigInteger> commitments) {
    this.commitment = commitment;
    this.commitments = commitments;
    this.ss = new HashMap<>();
  }

  @Override
  public Map<Integer, BigInteger> out() {
    return ss;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    int players = spdzResourcePool.getNoOfParties();
    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      // Send your opening to all players
      BigInteger value = this.commitment.getValue();
      network.sendToAll(serializer.serialize(value));
      BigInteger randomness = this.commitment.getRandomness();
      network.sendToAll(serializer.serialize(randomness));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      // Receive openings from all parties and check they are valid
      List<byte[]> values = network.receiveFromAll();
      List<byte[]> randomnesses = network.receiveFromAll();

      boolean openingValidated = true;
      BigInteger[] broadcastMessages = new BigInteger[2 * players];
      for (int i = 0; i < players; i++) {
        BigInteger commitment = commitments.get(i + 1);
        BigInteger open0 = serializer.deserialize(values.get(i));
        BigInteger open1 = serializer.deserialize(randomnesses.get(i));
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

  private boolean checkCommitment(SpdzResourcePool numericResourcePool, BigInteger commitment,
      BigInteger value, BigInteger randomness) {
    MessageDigest messageDigest = numericResourcePool.getMessageDigest();
    messageDigest.update(value.toByteArray());
    messageDigest.update(randomness.toByteArray());
    BigInteger testSubject = new BigInteger(messageDigest.digest())
        .mod(numericResourcePool.getModulus());
    return commitment.equals(testSubject);
  }
}
