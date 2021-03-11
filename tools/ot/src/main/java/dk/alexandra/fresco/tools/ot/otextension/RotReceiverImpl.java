package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.List;

public class RotReceiverImpl extends RotSharedImpl implements RotReceiver {
  private final CoteReceiver receiver;
  private final OtExtensionResourcePool resources;
  private final Network network;

  /**
   * Construct a receiving party for an instance of the random OT extension protocol.
   *
   * @param rec
   *          The correlated OT with error receiver this protocol will use
   * @param resources
   *          The common OT extension resources
   * @param network
   *          The network to use
   */
  public RotReceiverImpl(CoteReceiver rec, OtExtensionResourcePool resources,
      Network network) {
    super(resources.getCoinTossing(), resources.getDigest(), resources
        .getComputationalSecurityParameter());
    this.receiver = rec;
    this.resources = resources;
    this.network = network;
  }

  @Override
  public List<StrictBitVector> extend(StrictBitVector choices) {
    // The underlying scheme requires computational security parameter plus lambda security
    // parameter extra OTs
    int minOts = choices.getSize() + resources.getComputationalSecurityParameter() + resources
        .getLambdaSecurityParam();
    // Round up to nearest two-power, which is required by the underlying scheme
    int ellPrime = (int) Math.pow(2, Math.ceil(Math.log(minOts) / Math.log(2)));
    // Extend the choices with random choices for padding
    StrictBitVector paddingChoices = new StrictBitVector(ellPrime - choices.getSize(), resources
        .getRandomGenerator());
    StrictBitVector extendedChoices = StrictBitVector.concat(choices,
        paddingChoices);
    // Use the choices along with the random padding uses for correlated OT with
    // errors
    List<StrictBitVector> tlist = receiver.extend(extendedChoices);
    // Agree on challenges for linear combination test
    List<StrictBitVector> chiList = getChallenges(ellPrime);
    StrictBitVector xvec = computeBitLinearCombination(extendedChoices, chiList);
    network.send(resources.getOtherId(), xvec.toByteArray());
    StrictBitVector tvec = computeInnerProduct(chiList, tlist);
    network.send(resources.getOtherId(), tvec.toByteArray());
    // Remove the correlation of the OTs by hashing
    List<StrictBitVector> vvec = hashBitVector(tlist, choices.getSize());
    return vvec;
  }

  /**
   * Computes the sum of element in a list based on a vector if indicator
   * variables. The sum will be based on Galois addition in the binary extension
   * field of the individual elements of the list. That is, through an XOR
   * operation. <br>
   * All elements of the list MUST have equal size! And both the list and vector
   * of indicator bits MUST contain an equal amount of entries!
   *
   * @param indicators
   *          The vector of indicator bits
   * @param list
   *          The input list, with all elements of equal size
   * @return The inner product represented as a StrictBitVector
   */
  private static StrictBitVector computeBitLinearCombination(
      StrictBitVector indicators,
      List<StrictBitVector> list) {
    StrictBitVector res = new StrictBitVector(list.get(0).getSize());
    for (int i = 0; i < indicators.getSize(); i++) {
      if (indicators.getBit(i, false)) {
        res.xor(list.get(i));
      }
    }
    return res;
  }

}
