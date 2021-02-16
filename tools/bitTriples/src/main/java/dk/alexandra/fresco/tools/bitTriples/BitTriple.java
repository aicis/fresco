package dk.alexandra.fresco.tools.bitTriples;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.cointossing.CoinTossingMpc;
import dk.alexandra.fresco.tools.bitTriples.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrgImpl;
import dk.alexandra.fresco.tools.bitTriples.triple.TripleGeneration;
import java.util.List;

/**
 * Implementation of the main MASCOT protocol (<a href="https://eprint.iacr.org/2016/505.pdf">https://eprint.iacr.org/2016/505.pdf</a>)
 * which can be used for the SPDZ pre-processing phase. <br> Supports generation of multiplication
 * triples, random authenticated elements, and random authenticated bits.
 */
public class BitTriple {

  private final TripleGeneration tripleGeneration;
  //private final BitConverter bitConverter;
  private final BitTripleResourcePool resourcePool;

  /**
   * Creates new {@link BitTriple}.
   */
  public BitTriple(BitTripleResourcePool resourcePool, Network network, StrictBitVector macKeyShareLeft,StrictBitVector macKeyShareRight) {
    this.resourcePool = resourcePool;
    // agree on joint seed
    StrictBitVector jointSeed = new CoinTossingMpc(resourcePool, network)
        .generateJointSeed(resourcePool.getPrgSeedBitLength());
    BytePrg jointSampler = new BytePrgImpl(jointSeed);
    this.tripleGeneration =
        new TripleGeneration(
            resourcePool, network, resourcePool.getComputationalSecurityBitParameter(), jointSampler);
  }

  /**
   * Generates a batch of multiplication triples.
   *
   * @param numTriples number of triples in batch
   * @return multiplication triples
   */
  public List<MultiplicationTriple> getTriples(int numTriples) {
    return tripleGeneration.triple(numTriples);
  }

}
