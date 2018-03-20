package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Protocol class for the party acting as the receiver in an correlated OT with
 * errors extension.
 */
public class CoteReceiver extends CoteShared {
  private final OtExtensionResourcePool resources;
  private final Network network;
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
    super(resources.getInstanceId());
    this.prgs = new ArrayList<>(resources.getComputationalSecurityParameter());
    for (Pair<StrictBitVector, StrictBitVector> pair : resources.getSeedOts()
        .getSentMessages()) {
      Drbg prgZero = initPrg(pair.getFirst());
      Drbg prgOne = initPrg(pair.getSecond());
      prgs.add(new Pair<>(prgZero, prgOne));
    }
    this.resources = resources;
    this.network = network;
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
    int bytesNeeded = choices.getSize() / Byte.SIZE;
    final List<StrictBitVector>  tlistZero = prgs.parallelStream()
        .limit(resources.getComputationalSecurityParameter())
        .map(p -> p.getFirst())
        .map(drbg -> {
          byte[] byteBuffer = new byte[bytesNeeded];
          drbg.nextBytes(byteBuffer);
          return byteBuffer;
        })
        .map(StrictBitVector::new)
        .collect(Collectors.toList());
    final List<StrictBitVector> ulist = prgs.parallelStream()
        .limit(resources.getComputationalSecurityParameter())
        .map(p -> p.getSecond())
        .map(drbg -> {
          byte[] byteBuffer = new byte[bytesNeeded];
          drbg.nextBytes(byteBuffer);
          return byteBuffer;
        })
        .map(StrictBitVector::new)
        .collect(Collectors.toList());
    ulist.parallelStream().forEach(u -> u.xor(choices));
    IntStream.range(0, resources.getComputationalSecurityParameter()).parallel()
      .forEach(i -> ulist.get(i).xor(tlistZero.get(i)));
    sendList(ulist);
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
    network.send(resources.getOtherId(), toSend);
  }
}
