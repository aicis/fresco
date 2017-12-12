package dk.alexandra.fresco.tools.mascot.cointossing;

import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.commit.CommitmentBasedProtocol;

public class CoinTossingMpc extends CommitmentBasedProtocol<StrictBitVector> {

  private Random rand;

  /**
   * Creates new coin-tossing protocol.
   * 
   * @param ctx
   */
  public CoinTossingMpc(MascotContext ctx) {
    super(ctx, ctx.getSbvSerializer());
    this.rand = ctx.getRand();
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
    StrictBitVector ownSeed = new StrictBitVector(bitLengthSeed, rand);
    return generateJointSeed(ownSeed);
  }

  /**
   * Distribute seeds and recombine into single seed to create joint seed.
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
