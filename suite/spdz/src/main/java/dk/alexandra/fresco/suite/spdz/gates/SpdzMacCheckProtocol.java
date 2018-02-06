package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

public class SpdzMacCheckProtocol implements ProtocolProducer {

  private SequentialProtocolProducer protocolProducer;
  private SpdzOpenCommitProtocol openComm;
  private int openedValuesSize;

  /**
   * Protocol which handles the MAC check internal to SPDZ. If this protocol reaches the end, no
   * malicious activity was detected and the storage is reset.
   *
   * @param rand A secure randomness source
   * @param digest A secure hash used for the commitment scheme
   * @param storage The store containing the half-opened values to be checked
   * @param modulus The global modulus used.
   */
  public SpdzMacCheckProtocol(
      final SecureRandom rand,
      final MessageDigest digest,
      final SpdzStorage storage,
      final BigInteger modulus) {
    protocolProducer = new SequentialProtocolProducer();
    protocolProducer.append(new LazyProtocolProducerDecorator(() -> {
      BigInteger s = new BigInteger(modulus.bitLength(), rand).mod(modulus);
      SpdzCommitment commitment = new SpdzCommitment(digest, s, rand);
      SpdzCommitProtocol commitmentProtocol = new SpdzCommitProtocol(commitment);
      openComm = new SpdzOpenCommitProtocol(commitment, commitmentProtocol);

      return new SequentialProtocolProducer(
          new SingleProtocolProducer<>(commitmentProtocol),
          new SingleProtocolProducer<>(openComm));
    }));
    protocolProducer.append(new LazyProtocolProducerDecorator(() -> {
      List<BigInteger> openedValues = storage.getOpenedValues();

      // Add all s's to get the common random value:
      BigInteger s = BigInteger.ZERO;
      Map<Integer, BigInteger> commitments = openComm.out();
      for (BigInteger otherS : commitments.values()) {
        s = s.add(otherS);
      }

      openedValuesSize = openedValues.size();

      BigInteger[] rs = new BigInteger[openedValuesSize];
      BigInteger temporaryR = s;
      for (int i = 0; i < openedValuesSize; i++) {
        temporaryR = new BigInteger(digest.digest(temporaryR.toByteArray())).mod(modulus);
        rs[i] = temporaryR;
      }
      BigInteger a = BigInteger.ZERO;
      int index = 0;
      for (BigInteger aa : openedValues) {
        a = a.add(aa.multiply(rs[index++])).mod(modulus);
      }

      List<SpdzElement> closedValues = storage.getClosedValues();
      // compute gamma_i as the sum of all MAC's on the opened values times
      // r_j.
      if (closedValues.size() != openedValuesSize) {
        throw new MaliciousException(
            "Malicious activity detected: Amount of closed values does not "
                + "equal the amount of partially opened values. Aborting!");
      }
      BigInteger gamma = BigInteger.ZERO;
      index = 0;
      for (SpdzElement c : closedValues) {
        gamma = gamma.add(rs[index++].multiply(c.getMac())).mod(modulus);
      }

      BigInteger alpha = storage.getSecretSharedKey();
      // compute delta_i as: gamma_i - alpha_i*a
      BigInteger delta = gamma.subtract(alpha.multiply(a)).mod(modulus);
      // Commit to delta and open it afterwards
      SpdzCommitment commitment = new SpdzCommitment(digest, delta, rand);
      SpdzCommitProtocol commitmentProtocol = new SpdzCommitProtocol(commitment);
      openComm = new SpdzOpenCommitProtocol(commitment, commitmentProtocol);

      return new SequentialProtocolProducer(
          new SingleProtocolProducer<>(commitmentProtocol),
          new SingleProtocolProducer<>(openComm));
    }));
    protocolProducer.append(new LazyProtocolProducerDecorator(() -> {
      BigInteger deltaSum = BigInteger.ZERO;
      Map<Integer, BigInteger> commitments = openComm.out();
      for (BigInteger d : commitments.values()) {
        deltaSum = deltaSum.add(d);
      }
      deltaSum = deltaSum.mod(modulus);
      if (!deltaSum.equals(BigInteger.ZERO)) {
        throw new MaliciousException(
            "The sum of delta's was not 0. Someone was corrupting something amongst "
                + openedValuesSize
                + " macs. Sum was " + deltaSum.toString() + " Aborting!");
      }
      // clean up store before returning to evaluating such that we only
      // evaluate the next macs, not those we already checked.
      storage.reset();
      return new SequentialProtocolProducer();
    }));
  }

  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    protocolProducer.getNextProtocols(protocolCollection);
  }

  @Override
  public boolean hasNextProtocols() {
    return protocolProducer.hasNextProtocols();
  }
}
