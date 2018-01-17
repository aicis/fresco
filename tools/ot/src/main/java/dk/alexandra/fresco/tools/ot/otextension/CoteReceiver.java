package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol class for the party acting as the receiver in an correlated OT with
 * errors extension.
 */
public class CoteReceiver extends CoteShared {
  private final List<Pair<Drbg, Drbg>> prgs;

  /**
   * Constructs a correlated OT extension with errors receiver instance.
   *
   * @param resources
   *          The common resource pool needed for OT extension
   * @param network
   *          The network object used to communicate with the other party
   */
  public CoteReceiver(OtExtensionResourcePool resources, Network network) {
    super(resources, network);
    this.prgs = new ArrayList<>(getkBitLength());
    for (Pair<StrictBitVector, StrictBitVector> pair : resources.getSeedOts()
        .getSentMessages()) {
      Drbg prgZero = initPrg(pair.getFirst());
      Drbg prgFirst = initPrg(pair.getSecond());
      prgs.add(new Pair<>(prgZero, prgFirst));
    }
  }

  /**
   * Constructs a new batch of correlated OTs with errors.
   *
   * @param choices
   *          The receivers random choices for this extension. This MUST have
   *          size 2^x for some x >=3.
   * @return A list of pairs consisting of the bit choices, followed by the
   *         received messages
   */
  public List<StrictBitVector> extend(StrictBitVector choices) {
    if (choices.getSize() < 1) {
      throw new IllegalArgumentException(
          "The amount of OTs must be a positive integer");
    }
    // Compute how many bytes we need for "size" OTs by dividing "size" by 8
    // (the amount of bits in the primitive type; byte)
    int bytesNeeded = choices.getSize() / 8;
    // Use prgs to expand the seeds
    List<StrictBitVector> tlistZero = new ArrayList<>(getkBitLength());
    List<StrictBitVector> ulist = new ArrayList<>(getkBitLength());
    for (int i = 0; i < getkBitLength(); i++) {
      // Expand the seed OTs using a prg and store the result in tlistZero
      byte[] byteBuffer = new byte[bytesNeeded];
      prgs.get(i).getFirst().nextBytes(byteBuffer);
      StrictBitVector tzero = new StrictBitVector(byteBuffer);
      tlistZero.add(tzero);
      byteBuffer = new byte[bytesNeeded];
      prgs.get(i).getSecond().nextBytes(byteBuffer);
      // Compute the u list, i.e. tzero XOR tone XOR randomChoices
      // Note that this is an in-place call and thus tone gets modified
      StrictBitVector tone = new StrictBitVector(byteBuffer);
      tone.xor(tzero);
      tone.xor(choices);
      ulist.add(tone);
    }
    sendList(ulist);
    // Complete tilt-your-head by transposing the message "matrix"
    return Transpose.transpose(tlistZero);
  }


  /**
   * Sends a list of StrictBitVectors to the default (0) channel.
   *
   * @param list
   *          List to send, where all elements are required to have the same
   *          length.
   */
  private void sendList(List<StrictBitVector> list) {
    // Find the amount of bytes needed for each bitvector in the list
    int elementLength = list.get(0).getSize() / 8;
    // Allocate space for all elements in the list.
    byte[] toSend = new byte[(list.get(0).getSize() / 8) * list.size()];
    for (int i = 0; i < list.size(); i++) {
      System.arraycopy(list.get(i).toByteArray(), 0, toSend, i * elementLength,
          elementLength);
    }
    getNetwork().send(getOtherId(), toSend);
  }

}
