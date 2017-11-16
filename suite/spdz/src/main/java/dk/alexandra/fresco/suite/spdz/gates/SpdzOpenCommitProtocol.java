package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SpdzOpenCommitProtocol extends SpdzNativeProtocol<Map<Integer, BigInteger>> {

  private SpdzCommitment commitment;
  private Map<Integer, BigInteger> ss;
  private Map<Integer, BigInteger> commitments;
  private boolean openingValidated;
  private boolean done = false;
  private byte[] digest;

  public SpdzOpenCommitProtocol(SpdzCommitment commitment,
      Map<Integer, BigInteger> commitments, Map<Integer, BigInteger> ss) {
    this.commitment = commitment;
    this.commitments = commitments;
    this.ss = ss;
  }

  @Override
  public Map<Integer, BigInteger> out() {
    return ss;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    int players = spdzResourcePool.getNoOfParties();
    BigIntegerSerializer serializer = spdzResourcePool.getSerializer();
    switch (round) {
      case 0: // Send your opening to all players
        BigInteger value = this.commitment.getValue();
        network.sendToAll(serializer.toBytes(value));
        BigInteger randomness = this.commitment.getRandomness();
        network.sendToAll(serializer.toBytes(randomness));
        break;
      case 1: // Receive openings from all parties and check they are valid
        List<byte[]> values = network.receiveFromAll();
        List<byte[]> randomnesses = network.receiveFromAll();

        openingValidated = true;
        BigInteger[] broadcastMessages = new BigInteger[2 * players];
        for (int i = 0; i < players; i++) {
          BigInteger com = commitments.get(i + 1);
          BigInteger open0 = serializer.toBigInteger(values.get(i));
          BigInteger open1 = serializer.toBigInteger(randomnesses.get(i));
          boolean validate = checkCommitment(
              spdzResourcePool, com,
              open0, open1);
          openingValidated = openingValidated && validate;
          ss.put(i, open0);
          broadcastMessages[i * 2] = open0;
          broadcastMessages[i * 2 + 1] = open1;
        }
        if (players < 3) {
          if (!openingValidated) {
            throw new MPCException("Opening commitments failed.");
          }
          done = true;
        } else {
          digest = sendBroadcastValidation(
              spdzResourcePool.getMessageDigest(),
              network, Arrays.asList(broadcastMessages));
        }
        break;
      case 2: // If more than three players check if openings where
        // broadcasted correctly
        boolean validated = receiveBroadcastValidation(network, digest);
        if (!(validated && openingValidated)) {
          throw new MPCException("Opening commitments failed.");
        }
        done = true;
        break;
      default:
        throw new MPCException("No more rounds to evaluate.");
    }
    if (done) {
      return EvaluationStatus.IS_DONE;
    } else {
      return EvaluationStatus.HAS_MORE_ROUNDS;
    }
  }

  private boolean checkCommitment(SpdzResourcePool spdzResourcePool, BigInteger commitment,
      BigInteger value, BigInteger randomness) {
    MessageDigest messageDigest = spdzResourcePool.getMessageDigest();
    messageDigest.update(value.toByteArray());
    messageDigest.update(randomness.toByteArray());
    BigInteger testSubject = new BigInteger(messageDigest.digest())
        .mod(spdzResourcePool.getModulus());
    return commitment.equals(testSubject);
  }
}
