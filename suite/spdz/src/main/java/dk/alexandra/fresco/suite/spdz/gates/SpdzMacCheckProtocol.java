package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdzMacCheckProtocol implements ProtocolProducer {

  private SecureRandom rand;
  private MessageDigest digest;
  private List<BigInteger> as;
  private SpdzStorage storage;
  private int round = 0;
  private ProtocolProducer pp;
  private Map<Integer, BigInteger> commitments;
  private BigInteger modulus;

  public SpdzMacCheckProtocol(SecureRandom rand, MessageDigest digest, SpdzStorage storage,
      Map<Integer, BigInteger> commitments, BigInteger modulus) {
    this.rand = rand;
    this.digest = digest;
    this.storage = storage;
    this.commitments = commitments;
    if (commitments != null) {
      this.round = 1;
    } else {
      this.commitments = new HashMap<>();
    }
    this.modulus = modulus;
  }

  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    if (pp == null) {
      if (round == 0) {
        BigInteger s = new BigInteger(modulus.bitLength(), rand).mod(modulus);
        SpdzCommitment commitment = new SpdzCommitment(digest, s, rand);
        Map<Integer, BigInteger> comms = new HashMap<>();
        SpdzCommitProtocol comm = new SpdzCommitProtocol(commitment, comms);
        SpdzOpenCommitProtocol open = new SpdzOpenCommitProtocol(commitment, comms, commitments);

        pp = new SequentialProtocolProducer(
            new SingleProtocolProducer<>(comm),
            new SingleProtocolProducer<>(open));
      } else if (round == 1) {
        BigInteger alpha = storage.getSSK();
        this.as = storage.getOpenedValues();
        List<SpdzElement> closedValues = storage.getClosedValues();

        // Add all s's to get the common random value:
        BigInteger s = BigInteger.ZERO;
        for (BigInteger otherS : commitments.values()) {
          s = s.add(otherS);
        }

        int t = as.size();

        BigInteger[] rs = new BigInteger[t];
        BigInteger r_temp = s;
        for (int i = 0; i < t; i++) {
          r_temp = new BigInteger(digest.digest(r_temp.toByteArray())).mod(modulus);
          rs[i] = r_temp;
        }
        BigInteger a = BigInteger.ZERO;
        int index = 0;
        for (BigInteger aa : as) {
          a = a.add(aa.multiply(rs[index++])).mod(modulus);
        }

        // compute gamma_i as the sum of all MAC's on the opened values times
        // r_j.
        if (closedValues.size() != t) {
          throw new MPCException(
              "Amount of closed values does not equal the amount of partially opened values. Aborting!");
        }
        BigInteger gamma = BigInteger.ZERO;
        index = 0;
        for (SpdzElement c : closedValues) {
          gamma = gamma.add(rs[index++].multiply(c.getMac())).mod(modulus);
        }

        // compute delta_i as: gamma_i - alpha_i*a
        BigInteger delta = gamma.subtract(alpha.multiply(a)).mod(modulus);
        // Commit to delta and open it afterwards
        SpdzCommitment commitment = new SpdzCommitment(digest, delta, rand);
        Map<Integer, BigInteger> comms = new HashMap<>();
        SpdzCommitProtocol comm = new SpdzCommitProtocol(commitment, comms);
        commitments = new HashMap<>();
        SpdzOpenCommitProtocol open = new SpdzOpenCommitProtocol(commitment, comms, commitments);

        pp = new SequentialProtocolProducer(
            new SingleProtocolProducer<>(comm),
            new SingleProtocolProducer<>(open));
      } else if (round == 2) {
        BigInteger deltaSum = BigInteger.ZERO;
        for (BigInteger d : commitments.values()) {
          deltaSum = deltaSum.add(d);
        }
        deltaSum = deltaSum.mod(modulus);
        if (!deltaSum.equals(BigInteger.ZERO)) {
          throw new MPCException(
              "The sum of delta's was not 0. Someone was corrupting something amongst " + as.size()
                  + " macs. Sum was " + deltaSum.toString() + " Aborting!");
        }
        // clean up store before returning to evaluating such that we only
        // evaluate the next macs, not those we already checked.
        this.storage.reset();
        pp = new SequentialProtocolProducer();
      }
    }
    if (pp.hasNextProtocols()) {
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
