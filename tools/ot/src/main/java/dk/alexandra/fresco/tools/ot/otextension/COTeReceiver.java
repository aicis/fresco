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
    // Make monochrome vectors
    List<byte[]> monoVec = makeMonoVec(randomChoices);
    for (int i = 0; i < kBitLength; i++) {
      // Expand the seed OTs using a prg
      byte[] tZero = new byte[bytesNeeded];
      byte[] tFirst = new byte[bytesNeeded];
      prgs.get(i).getFirst().nextBytes(tZero);
      prgs.get(i).getSecond().nextBytes(tFirst);
      tVecZero.add(tZero);
      // Compute the u vector, i.e. tZero XOR tFirst XOR MonoVal
      // Note that this is an in-place call and thus tFirst gets modified
      xor(tFirst, tZero);
      xor(tFirst, monoVec.get(i));
      uVec.add(tFirst);
    }
    sendList(uVec);
    // Complete tilt-your-head by transposing the message "matrix"
    Transpose.transpose(tVecZero);
    return tVecZero;
  }

  /**
   * Construct the monochrome matrix, including the necessary transposition.
   * 
   * @param randomChoices
   *          The random choices which the matrix must be based on
   * @return The matrix based on the transposition of the random monochrome
   *         vectors
   */
  protected List<byte[]> makeMonoVec(byte[] randomChoices) {
    List<byte[]> monoVec = new ArrayList<>(randomChoices.length * 8);
    for (int i = 0; i < randomChoices.length * 8; i++) {
      byte[] monoVal = makeMonoVal(getBit(randomChoices, i), kBitLength / 8);
      monoVec.add(monoVal);
    }
    Transpose.transpose(monoVec);
    return monoVec;
  }

  /**
   * Makes a monochrome byte array of "size" bytes, based on the boolean "bit"
   * 
   * @param bit
   *          Boolean to base monochrome vector on
   * @param size
   *          The amount of bytes in the result vector
   * @return Monochrome byte array
   */
  // The method is protected instead of private to allow testing
  protected static byte[] makeMonoVal(boolean bit, int size) {
    byte[] res = new byte[size];
    if (bit == true) {
      for (int i = 0; i < size; i++) {
        // Since byte is a signed value represented using two's complement -1
        // will assign it to the all 1 bit string
        res[i] = (byte) 0xFF;
      }
    }
    // else the array will contain all 0 bits as this is the default value of
    // the primitive type byte
    return res;
  }
}
