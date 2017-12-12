package dk.alexandra.fresco.tools.mascot.cointossing;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.commit.CommitmentBasedProtocol;

public class CoinTossingMpc extends CommitmentBasedProtocol<StrictBitVector> {

  private Random rand;

  /**
   * Creates new coin-tossing protocol.
   * 
   * @param resourcePool
   */
  public CoinTossingMpc(MascotResourcePool resourcePool, Network network) {
    super(resourcePool, network, resourcePool.getStrictBitVectorSerializer());
    this.rand = new SecureRandom();
  }

  /**
   * Computes all parties seeds into one by xoring.
   * 
   * @param seeds
   * @return
   */
  StrictBitVector combine(List<StrictBitVector> seeds) {
    StrictBitVector acc = seeds.get(0);
    for (StrictBitVector seed : seeds.subList(1, seeds.size())) {
      acc.xor(seed);
    }
    return acc;
  }

  /**
   * Generates random seed and calls {@link CoinTossingMpc#generateJointSeed(StrictBitVector)}.
   * 
   * @param byteLength
   * @return
   */
  public StrictBitVector generateJointSeed(int bitLengthSeed) {
    // generate own seed
    return generateJointSeed(new StrictBitVector(bitLengthSeed, rand));
  }

  /**
   * Distribute seeds and combine into single, joint seed.
   * 
   * @param byteLength the length of the seed to be generated
   * @return
   */
  public StrictBitVector generateJointSeed(StrictBitVector ownSeed) {
    // distribute seeds
    List<StrictBitVector> allSeeds = allCommit(ownSeed);
    // combine seeds
    return combine(allSeeds);
  }

}
