package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol class for the party acting as the sender in an correlated OT with
 * errors extension.
 * 
 * @author jot2re
 *
 */
public class CoteSender extends CoteShared {
  // The prgs based on the seeds learned from OT
  private List<Drbg> prgs;
  // The random messages choices for the random seed OTs
  private StrictBitVector otChoices;

  /**
   * Construct a sending party for an instance of the correlated OT protocol.
   * 
   * @param otherId
   *          The ID of the receiving party
   * @param kbitLength
   *          Computational security parameter. Must be a positive number
   *          divisible by 8
   * @param lambdaSecurityParam
   *          The statistical security parameter. Must be positive.
   * @param rand
   *          The cryptographically secure and private randomness generator of
   *          this party. Must not be null and must be initialized.
   * @param network
   *          The network interface. Must not be null and must be initialized.
   */
  public CoteSender(int myId, int otherId, int kbitLength,
      int lambdaSecurityParam, Drbg rand, Network network) {
    super(myId, otherId, kbitLength, lambdaSecurityParam, rand, network);
    this.prgs = new ArrayList<>(kbitLength);
  }

  /**
   * Initialize the correlated OT with errors extension. This should only be
   * called once as it completes extensive seed OTs.
   * 
   * @throws FailedOtExtensionException
   *           Thrown if the PRG algorithm used does not exist
   * @throws MaliciousOtExtensionException
   *           Thrown in case the other party cheats in the seed OTs
   */
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    this.otChoices = new StrictBitVector(getkBitLength(), getRand());
    // Complete the seed OTs acting as the receiver (NOT the sender)
    for (int i = 0; i < getkBitLength(); i++) {
      StrictBitVector message = ot.receive(otChoices.getBit(i, false));
      // Initialize the PRGs with the random messages
      Drbg prg = makePrg(message);
      prgs.add(prg);
    }
    initialized = true;
  }

  /**
   * Returns a clone of the random bit choices used for OT.
   * 
   * @return A clone of the OT choices
   */
  public StrictBitVector getDelta() {
    // Return a new copy to avoid issues in case the caller modifies the bit
    // vector
    return new StrictBitVector(otChoices.toByteArray(), getkBitLength());
  }

  /**
   * Constructs a new batch of correlated OTs with errors.
   * 
   * @param size
   *          Amount of OTs to construct
   */
  public List<StrictBitVector> extend(int size) {
    if (size < 1) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer");
    }
    if (size % 8 != 0) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer divisize by 8");
    }
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte)
    int bytesNeeded = size / 8;
    byte[] byteBuffer = new byte[bytesNeeded];
    List<StrictBitVector> tlist = new ArrayList<>(getkBitLength());
    for (int i = 0; i < getkBitLength(); i++) {
      // Expand the message learned from the seed OTs using a PRG
      prgs.get(i).nextBytes(byteBuffer);
      StrictBitVector tvec = new StrictBitVector(byteBuffer, size);
      tlist.add(tvec);
    }
    List<StrictBitVector> ulist = receiveList(getkBitLength());
    // Update tlist based on the random choices from the seed OTs, i.e
    // tlist[i] := (otChoicesp[i] AND ulist[i]) XOR tlist[i]
    for (int i = 0; i < getkBitLength(); i++) {
      if (otChoices.getBit(i, false) == true) {
        tlist.get(i).xor(ulist.get(i));
      }
    }
    // Complete tilt-your-head by transposing the message "matrix"
    return Transpose.transpose(tlist);
  }

}
