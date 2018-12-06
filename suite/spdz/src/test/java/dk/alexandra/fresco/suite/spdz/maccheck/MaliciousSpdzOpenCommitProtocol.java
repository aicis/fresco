package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MaliciousSpdzOpenCommitProtocol extends SpdzNativeProtocol<Boolean> {

  private SpdzCommitment commitment;
  private Map<Integer, BigIntegerI> ss;
  private Map<Integer, BigIntegerI> commitments;
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
      Map<Integer, BigIntegerI> commitments, Map<Integer, BigIntegerI> ss, boolean corruptNow) {
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
    ByteSerializer<BigIntegerI> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      // Send your opening to all players
      BigIntegerI value = this.commitment.getValue();
      network.sendToAll(serializer.serialize(value));
      BigIntegerI randomness = this.commitment.getRandomness();
      if (corruptNow) {
        randomness = randomness
            .add(BigInt.fromConstant(BigInteger.ONE, spdzResourcePool.getModulus()));
      }
      network.sendToAll(serializer.serialize(randomness));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else if (round == 1) {
      // Receive openings from all parties and check they are valid
      List<byte[]> values = network.receiveFromAll();
      List<byte[]> randomnesses = network.receiveFromAll();

      openingValidated = true;
      BigIntegerI[] broadcastMessages = new BigIntegerI[2 * players];
      for (int i = 0; i < players; i++) {
        BigIntegerI com = commitments.get(i + 1);
        BigIntegerI open0 = serializer.deserialize(values.get(i));
        BigIntegerI open1 = serializer.deserialize(randomnesses.get(i));
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

  private boolean checkCommitment(SpdzResourcePool spdzResourcePool, BigIntegerI commitment,
      BigIntegerI value, BigIntegerI randomness) {
    ByteSerializer<BigIntegerI> serializer = spdzResourcePool.getSerializer();
    MessageDigest messageDigest = spdzResourcePool.getMessageDigest();
    messageDigest.update(serializer.serialize(value));
    messageDigest.update(serializer.serialize(randomness));
    BigIntegerI testSubject =
        serializer.deserialize(messageDigest.digest());
    return commitment.equals(testSubject);
  }

  private byte[] sendMaliciousBroadcastValidation(MessageDigest dig, Network network,
      Collection<BigIntegerI> bs) {
    for (BigIntegerI b : bs) {
      dig.update(b.asBigInteger().toByteArray());
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

