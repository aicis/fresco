package dk.alexandra.fresco.tools.bitTriples.maccheck;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.bitTriples.utils.VectorOperations;
import java.util.List;

public class MacCheck {

  private final Network network;
  private final BitTripleResourcePool resourcePool;
  private final BytePrg jointSampler;

  public MacCheck(BitTripleResourcePool resourcePool, Network network, BytePrg jointSampler) {
    this.resourcePool = resourcePool;
    this.network = network;
    this.jointSampler = jointSampler;
  }

  /**
   * Runs protocol described in fig. 16.
   * @param publicValues The public values
   * @param macShares the mac values
   * @param myMac the mac of the party
   * @return true if mac check was accepted
   */
  public boolean check(
      StrictBitVector publicValues, List<StrictBitVector> macShares, StrictBitVector myMac) {

    //Step 4
    StrictBitVector randomElement = jointSampler.getNext(publicValues.getSize());

    //Step 5
    boolean b = VectorOperations.sum(VectorOperations.and(randomElement,publicValues));
    //Step 6
    StrictBitVector sigma = VectorOperations.sum(VectorOperations.multiply(macShares, randomElement));
    sigma.xor(VectorOperations.multiply(myMac, b));

    // step 7-9
    StrictBitVector openSigma = VectorOperations.openVector(sigma,resourcePool,network);
    if (!VectorOperations.isZero(openSigma)) {
      throw new MaliciousException("Mac check failed");
    }
    return true;
  }
}
