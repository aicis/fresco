package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.Ot;
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
  private final List<Drbg> prgs;
  // The random messages choices for the random seed OTs
  private StrictBitVector otChoices;
  // The functionality for the underlying seed OTs
  private final Ot ot;

  /**
   * Construct a sending party for an instance of the correlated OT protocol.
   * 
   * @param resources
   *          The common resource pool needed for OT extension
   * @param network
   *          The network interface. Must not be null and must be initialized.
   * @param ot
   *          The OT functionality to use for seed OTs
   */
  public CoteSender(OtExtensionResourcePool resources, Network network, Ot ot) {
    super(resources, network);
    this.ot = ot;
    this.prgs = new ArrayList<>(resources.getComputationalSecurityParameter());
  }

  /**
   * Initialize the correlated OT with errors extension. This should only be
   * called once as it completes extensive seed OTs.
   */
  @Override
  public void initialize() {
    if (isInitialized()) {
      throw new IllegalStateException("Already initialized");
    }
    this.otChoices = new StrictBitVector(getkBitLength(), getRand());
    // Complete the seed OTs acting as the receiver (NOT the sender)
    for (int i = 0; i < getkBitLength(); i++) {
      StrictBitVector message = ot.receive(otChoices.getBit(i, false));
      // Initialize the PRGs with the random messages
      // TODO make sure this is okay!
      Drbg prg = new PaddingAesCtrDrbg(message.toByteArray(), 256);
      prgs.add(prg);
    }
    super.initialize();
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
    if (!isInitialized()) {
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

  /**
   * Receives a list of StrictBitVectors from the default (0) channel
   * 
   * @param size
   *          Amount of elements in vector to receive. All of which must be of
   *          equal size.
   * @return The list of received elements, or null in case an error occurred.
   */
  private List<StrictBitVector> receiveList(int size) {
    List<StrictBitVector> list = new ArrayList<>(size);
    byte[] byteBuffer = getNetwork().receive(getOtherId());
    int elementLength = byteBuffer.length / size;
    for (int i = 0; i < size; i++) {
      byte[] currentVector = new byte[elementLength];
      System.arraycopy(byteBuffer, i * elementLength, currentVector, 0,
          elementLength);
      StrictBitVector currentArr = new StrictBitVector(currentVector,
          elementLength * 8);
      list.add(currentArr);
    }
    return list;
  }
}
