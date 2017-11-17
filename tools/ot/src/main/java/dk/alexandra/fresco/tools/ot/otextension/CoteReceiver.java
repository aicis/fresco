package dk.alexandra.fresco.tools.ot.otextension;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;

public class CoteReceiver extends CoteShared {
  // Random messages used for the seed OTs
  private List<Pair<BigInteger, BigInteger>> seeds;
  private List<Pair<SecureRandom, SecureRandom>> prgs;

  /**
   * Constructs a correlated OT extension with errors receiver instance.
   * 
   * @param otherId
   *          ID of the other party to execute with
   * @param kbitLength
   *          The computational security parameter
   * @param lambdaSecurityParam
   *          The statistical security parameter
   * @param rand
   *          The current party's cryptographically secure randomness generator
   * @param network
   *          The network object used to communicate with the other party
   */
  public CoteReceiver(int otherId, int kbitLength, int lambdaSecurityParam,
      Random rand, Network network) {
    super(otherId, kbitLength, lambdaSecurityParam, rand, network);
    this.seeds = new ArrayList<>(kbitLength);
    this.prgs = new ArrayList<>(kbitLength);
  }

  /**
   * Initializes the correlated OT extension with errors by running true seed
   * OTs. This should only be done once for a given sender/receiver pair.
   * 
   * @throws NoSuchAlgorithmException
   *           Thrown if the underlying PRG algorithm does not exist.
   */
  public void initialize() throws NoSuchAlgorithmException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // Complete the seed OTs acting as the sender (NOT the receiver)
    for (int i = 0; i < kbitLength; i++) {
      BigInteger seedZero = new BigInteger(kbitLength, rand);
      BigInteger seedFirst = new BigInteger(kbitLength, rand);
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
    if (randomChoices.length != size / 8) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer divisize by 8");
    }
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte), rounding up
    int bytesNeeded = size / 8;
    // Use prgs to expand the seeds
    List<byte[]> tvecZero = new ArrayList<>(kbitLength);
    // u vector
    List<byte[]> uvec = new ArrayList<>(kbitLength);
    for (int i = 0; i < kbitLength; i++) {
      // Expand the seed OTs using a prg
      byte[] tzero = new byte[bytesNeeded];
      byte[] tone = new byte[bytesNeeded];
      prgs.get(i).getFirst().nextBytes(tzero);
      prgs.get(i).getSecond().nextBytes(tone);
      tvecZero.add(tzero);
      // Compute the u vector, i.e. tZero XOR tFirst XOR randomChoices
      // Note that this is an in-place call and thus tFirst gets modified
      xor(tone, tzero);
      xor(tone, randomChoices);
      uvec.add(tone);
    }
    sendList(uvec);
    // Complete tilt-your-head by transposing the message "matrix"
    return Transpose.transpose(tvecZero);
  }
}
