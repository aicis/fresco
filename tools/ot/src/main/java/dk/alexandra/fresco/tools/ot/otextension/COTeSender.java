package dk.alexandra.fresco.tools.ot.otextension;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;

/**
 * Protocol class for the party acting as the sender in an correlated OT with
 * errors extension.
 * 
 * @author jot2re
 *
 */
public class COTeSender extends COTeShared {

  // Random messages used for the seed OTs
  private List<Pair<BigInteger, BigInteger>> seeds;
  private List<Pair<SecureRandom, SecureRandom>> prgs;

  public COTeSender(int otherID, int kBitLength, int lambdaSecurityParam,
      Random rand, Network network) {
    super(otherID, kBitLength, lambdaSecurityParam, rand, network);
    this.seeds = new ArrayList<>(kBitLength);
    this.prgs = new ArrayList<>(kBitLength);
  }

  /**
   * Initialize the correlated OT with errors extension. This should only be
   * called once as it completes extensive seed OTs.
   */
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    for (int i = 0; i < kBitLength; i++) {
      BigInteger seedZero = new BigInteger(kBitLength, rand);
      BigInteger seedFirst = new BigInteger(kBitLength, rand);
//      ot.send(seedZero, seedFirst);
      seeds.add(new Pair<>(seedZero, seedFirst));
      // Initialize the PRGs with the random messages
      SecureRandom prgZero = new SecureRandom(seedZero.toByteArray());
      SecureRandom prgFirst = new SecureRandom(seedFirst.toByteArray());
      prgs.add(new Pair<>(prgZero, prgFirst));
    }
    initialized = true;
  }

  /**
   * Constructs a new batch of correlated OTs with errors. 
   * @param size Amount of OTs to construct
   */
  public void extend(int size) {
    if (size < 1) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer");
    }
    if (!initialized) {
      initialize();
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte), rounding up
    int bytesNeeded = (size + 8 - 1) / 8;
    // Use a prg to expand the seeds
    List<Pair<byte[], byte[]>> tVectors = new ArrayList<>(kBitLength);
    for (int i = 0; i < kBitLength; i++) {
//      prgs.get(i).getFirst().nextBytes(size);
    }
    
  }
}
