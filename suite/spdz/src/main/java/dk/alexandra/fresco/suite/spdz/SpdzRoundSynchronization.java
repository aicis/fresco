package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollectionList;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.suite.ProtocolSuite.RoundSynchronization;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.gates.SpdzCommitProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMacCheckProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOpenCommitProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzStorage;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A default implementation of the round synchronization for spdz - mostly doing
 * the MAC check if needed.
 */
public class SpdzRoundSynchronization implements RoundSynchronization<SpdzResourcePool> {

  private static final int macCheckThreshold = 100000;

  private boolean commitDone = false;
  private boolean openDone = false;
  private int roundNumber = 0;
  private SpdzCommitProtocol commitProtocol;
  private SpdzOpenCommitProtocol openProtocol;

  private void MACCheck(SpdzResourcePool resourcePool,
      SCENetwork sceNetworks) throws IOException {

    SpdzStorage storage = resourcePool.getStore();

    SpdzMacCheckProtocol macCheck = new SpdzMacCheckProtocol(
        resourcePool.getSecureRandom(),
        resourcePool.getMessageDigest(),
        storage,
        null, resourcePool.getModulus());

    int batchSize = 128;

    do {
      ProtocolCollectionList protocolCollectionList =
          new ProtocolCollectionList(batchSize);
      macCheck.getNextProtocols(protocolCollectionList);

      BatchedStrategy.processBatch(protocolCollectionList, sceNetworks, 0, resourcePool);
    } while (macCheck.hasNextProtocols());

    //reset boolean value
    resourcePool.setOutputProtocolInBatch(false);
    commitDone = false;
    openDone = false;
    roundNumber = 0;
  }

  @Override
  public void finishedEval(SpdzResourcePool resourcePool, SCENetwork sceNetwork) {
    try {
      MACCheck(resourcePool, sceNetwork);
    } catch (IOException e) {
      throw new MPCException("Could not complete MACCheck.", e);
    }
  }

  @Override
  public void finishedBatch(int gatesEvaluated, SpdzResourcePool resourcePool,
      SCENetwork sceNetwork)
      throws MPCException {
    gatesEvaluated += gatesEvaluated;
    if (gatesEvaluated > macCheckThreshold || resourcePool.isOutputProtocolInBatch()) {
      try {
        MACCheck(resourcePool, sceNetwork);
      } catch (IOException e) {
        throw new MPCException("Could not complete MACCheck.", e);
      }
    }
  }

  @Override
  public boolean roundFinished(int round, SpdzResourcePool resourcePool, SCENetwork sceNetwork)
      throws MPCException {
    if (resourcePool.isOutputProtocolInBatch()) {
      checkInit(resourcePool);

      if (!commitDone) {
        NativeProtocol.EvaluationStatus evaluate = commitProtocol
            .evaluate(round, resourcePool, sceNetwork);
        roundNumber = round + 1;
        commitDone = evaluate.equals(NativeProtocol.EvaluationStatus.IS_DONE);
        return false;
      }
      if (!openDone) {
        NativeProtocol.EvaluationStatus evaluate = openProtocol
            .evaluate(round - roundNumber, resourcePool, sceNetwork);
        openDone = evaluate.equals(NativeProtocol.EvaluationStatus.IS_DONE);
      }
      return openDone;
    }
    return true;
  }

  private void checkInit(SpdzResourcePool resourcePool) {
    BigInteger modulus = resourcePool.getModulus();
    Random rand = resourcePool.getRandom();
    MessageDigest messageDigest = resourcePool.getMessageDigest();

    BigInteger s = new BigInteger(modulus.bitLength(), rand).mod(modulus);
    SpdzCommitment commitment = new SpdzCommitment(messageDigest, s, rand);
    Map<Integer, BigInteger> comms = new HashMap<>();
    commitProtocol = new SpdzCommitProtocol(commitment, comms);
    Map<Integer, BigInteger> commitments = new HashMap<>();
    openProtocol = new SpdzOpenCommitProtocol(commitment, comms, commitments);
  }
}
