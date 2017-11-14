package dk.alexandra.fresco.tools.ot.otextension;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;

public class COTeReceiver extends COTeShared {
  // Random messages used for the seed OTs
  private List<Pair<BigInteger, BigInteger>> seeds;
  private List<Pair<SecureRandom, SecureRandom>> prgs;

  public COTeReceiver(int otherID, int kBitLength, int lambdaSecurityParam,
      Random rand, Network network) {
    super(otherID, kBitLength, lambdaSecurityParam, rand, network);
    this.seeds = new ArrayList<>(kBitLength);
    this.prgs = new ArrayList<>(kBitLength);
  }

  public void initialize() throws NoSuchAlgorithmException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // Complete the seed OTs acting as the sender (NOT the receiver)
    for (int i = 0; i < kBitLength; i++) {
      BigInteger seedZero = new BigInteger(kBitLength, rand);
      BigInteger seedFirst = new BigInteger(kBitLength, rand);
      ot.send(seedZero, seedFirst);
      seeds.add(new Pair<>(seedZero, seedFirst));
      // Initialize the PRGs with the random messages
      SecureRandom prgZero = SecureRandom.getInstance("SHA1PRNG");
      prgZero.setSeed(seedZero.toByteArray());
      SecureRandom prgFirst = SecureRandom.getInstance("SHA1PRNG");
      prgFirst.setSeed(seedFirst.toByteArray());
      prgs.add(new Pair<>(prgZero, prgFirst));
    }
    initialized = true;
  }

  /**
   * Constructs a new batch of correlated OTs with errors.
   * 
   * @param size
   *          Amount of OTs to construct
   * @return A list of pairs consisting of the bit choices, followed by the
   *         received messages
   */
  public List<byte[]> extend(byte[] randomChoices, int size) {
    if (size < 1) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer");
    }
    if (randomChoices.length != size / 8)
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer divisize by 8");
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte), rounding up
    int bytesNeeded = size / 8;
    // Use prgs to expand the seeds
    List<byte[]> tVecZero = new ArrayList<>(kBitLength);
    // u vector
    List<byte[]> uVec = new ArrayList<>(kBitLength);
    for (int i = 0; i < kBitLength; i++) {
      // Expand the seed OTs using a prg
      byte[] tZero = new byte[bytesNeeded];
      byte[] tOne = new byte[bytesNeeded];
      prgs.get(i).getFirst().nextBytes(tZero);
      prgs.get(i).getSecond().nextBytes(tOne);
      tVecZero.add(tZero);
      // Compute the u vector, i.e. tZero XOR tFirst XOR randomChoices
      // Note that this is an in-place call and thus tFirst gets modified
      xor(tOne, tZero);
      xor(tOne, randomChoices);
      uVec.add(tOne);
    }
    sendList(uVec);
    // Complete tilt-your-head by transposing the message "matrix"
    return Transpose.transpose(tVecZero);
  }
}
