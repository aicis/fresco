package dk.alexandra.fresco.tools.ot.otextension;

import java.math.BigInteger;
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

  public void initialize() {
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
      SecureRandom prgZero = new SecureRandom(seedZero.toByteArray());
      SecureRandom prgFirst = new SecureRandom(seedFirst.toByteArray());
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
  public List<Pair<Boolean, byte[]>> extend(int size) {
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
    // Use prgs to expand the seeds
    List<Pair<byte[], byte[]>> tPairs = new ArrayList<>(kBitLength);
    // u vector
    List<byte[]> uVec = new ArrayList<>(kBitLength);
    for (int i = 0; i < kBitLength; i++) {
      // Expand the seed OTs using a prg
      byte[] tZero = new byte[bytesNeeded];
      byte[] tFirst = new byte[bytesNeeded];
      prgs.get(i).getFirst().nextBytes(tZero);
      prgs.get(i).getFirst().nextBytes(tFirst);
      Pair<byte[], byte[]> tPair = new Pair<byte[], byte[]>(tZero, tFirst);
      tPairs.add(tPair);

      // Compute the u vector, i.e. tZero XOR tFirst XOR MonoVal
      byte[] temp = xor(tZero, tFirst);
      // Samples a random monochrome vector
      byte[] monoVal = makeMonoVal(rand.nextBoolean(), bytesNeeded);
      byte[] uVal = xor(temp, monoVal);
      uVec.add(uVal);
    }
    sendList(uVec);
    // Complete tilt-your-head by transposing the message "matrix"
    List<Pair<Boolean, byte[]>> messages = new ArrayList<>(size);
    // TODO
    return messages;
  }

  /**
   * Makes a monochrome byte array of "size" bytes, based on the boolean "bit"
   * 
   * @param bit
   *          Boolean to base monochrome vector on
   * @return Monochrome byte array
   */
  private byte[] makeMonoVal(boolean bit, int size) {
    byte[] res = new byte[size];
    if (bit == true) {
      for (int i = 0; i < size; i++) {
        // Since byte is a signed value represented using two's complement -1
        // will assign it to the all 1 bit string
        res[i] = -1;
      }
    }
    // else the array will contain all 0 bits as this is the default value of
    // the primitive type byte
    return res;
  }
}
