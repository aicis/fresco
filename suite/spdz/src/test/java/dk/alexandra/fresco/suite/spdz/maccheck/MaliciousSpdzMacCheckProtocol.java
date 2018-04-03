package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaliciousSpdzMacCheckProtocol implements ProtocolProducer {

  private SecureRandom rand;
  private MessageDigest digest;
  private List<BigInteger> as;
  private int round = 0;
  private ProtocolProducer pp;
  private Map<Integer, BigInteger> commitments;
  private BigInteger modulus;
  private MaliciousSpdzCommitProtocol comm;
  private MaliciousSpdzOpenCommitProtocol openComm;
  private final List<SpdzSInt> closedValues;
  private final List<BigInteger> openedValues;
  private final BigInteger alpha;

  public static boolean corruptCommitRound1 = false;
  public static boolean corruptOpenCommitRound1 = false;
  public static boolean corruptCommitRound2 = false;
  public static boolean corruptOpenCommitRound2 = false;

  public MaliciousSpdzMacCheckProtocol(
      final SecureRandom rand,
      final MessageDigest digest,
      final Pair<List<SpdzSInt>, List<BigInteger>> toCheck,
      final BigInteger modulus,
      final Drbg jointDrbg,
      final BigInteger alpha) {
    this.rand = rand;
    this.digest = digest;
    this.closedValues = toCheck.getFirst();
    this.openedValues = toCheck.getSecond();
    this.modulus = modulus;
    this.alpha = alpha;
  }


  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    if (pp == null) {
      if (round == 0) {
        BigInteger s = new BigInteger(modulus.bitLength(), rand).mod(modulus);
        SpdzCommitment commitment = new SpdzCommitment(digest, s, rand);
        Map<Integer, BigInteger> comms = new HashMap<>();
        comm = new MaliciousSpdzCommitProtocol(commitment, comms, corruptCommitRound1);
        openComm = new MaliciousSpdzOpenCommitProtocol(commitment, comms, commitments,
            corruptOpenCommitRound1);
        pp = new SequentialProtocolProducer(
            Arrays.asList(
                new SingleProtocolProducer<>(comm),
                new SingleProtocolProducer<>(openComm)));
      } else if (round == 1) {
        if (!comm.out()) {
          throw new MaliciousException(
              "Malicious activity detected: SecureBroadcastUtil of commitments was not validated.");
        }
        if (!openComm.out()) {
          throw new MaliciousException("Malicious activity detected: Opening commitments failed.");
        }

        this.as = openedValues;

        // Add all s's to get the common random value:
        BigInteger s = BigInteger.ZERO;
        for (BigInteger otherS : commitments.values()) {
          s = s.add(otherS);
        }

        int t = as.size();

        BigInteger[] rs = new BigInteger[t];
        BigInteger temporaryR = s;
        for (int i = 0; i < t; i++) {
          temporaryR = new BigInteger(digest.digest(temporaryR.toByteArray())).mod(modulus);
          rs[i] = temporaryR;
        }
        BigInteger a = BigInteger.ZERO;
        int index = 0;
        for (BigInteger aa : as) {
          a = a.add(aa.multiply(rs[index++])).mod(modulus);
        }

        // compute gamma_i as the sum of all MAC's on the opened values times
        // r_j.
        BigInteger gamma = BigInteger.ZERO;
        index = 0;
        for (SpdzSInt c : closedValues) {
          gamma = gamma.add(rs[index++].multiply(c.getMac())).mod(modulus);
        }

        // compute delta_i as: gamma_i - alpha_i*a
        BigInteger delta = gamma.subtract(alpha.multiply(a)).mod(modulus);
        // Commit to delta and open it afterwards
        SpdzCommitment commitment = new SpdzCommitment(digest, delta, rand);
        Map<Integer, BigInteger> comms = new HashMap<>();
        comm = new MaliciousSpdzCommitProtocol(commitment, comms, corruptCommitRound2);
        commitments = new HashMap<>();
        openComm = new MaliciousSpdzOpenCommitProtocol(commitment, comms, commitments,
            corruptOpenCommitRound2);
        pp = new SequentialProtocolProducer(
            Arrays.asList(
                new SingleProtocolProducer<>(comm),
                new SingleProtocolProducer<>(openComm)));
      } else {
        if (!comm.out()) {
          throw new MaliciousException(
              "Malicious activity detected: SecureBroadcastUtil of commitments was not validated.");
        }
        if (!openComm.out()) {
          throw new MaliciousException("Malicious activity detected: Opening commitments failed.");
        }
        BigInteger deltaSum = BigInteger.ZERO;
        for (BigInteger d : commitments.values()) {
          deltaSum = deltaSum.add(d);
        }
        deltaSum = deltaSum.mod(modulus);
        if (!deltaSum.equals(BigInteger.ZERO)) {
          throw new MaliciousException(
              "The sum of delta's was not 0. Someone was corrupting something amongst " + as.size()
                  + " macs. Sum was " + deltaSum.toString() + " Aborting!");
        }
        // clean up store before returning to evaluating such that we only
        // evaluate the next macs, not those we already checked.
        openedValues.clear();
        closedValues.clear();
        pp = null;
      }
    }
    if (pp != null && pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    } else {
      round++;
      pp = null;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return round < 3;
  }
}
