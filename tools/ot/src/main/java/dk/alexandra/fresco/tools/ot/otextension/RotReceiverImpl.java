package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import java.util.List;

public class RotReceiverImpl extends RotSharedImpl implements RotReceiver {
  private final CoteReceiver receiver;

  /**
   * Construct a receiving party for an instance of the random OT extension protocol.
   *
   * @param rec
   *          The correlated OT with error receiver this protocol will use
   * @param ct
   *          The coin tossing instance to use
   */
  public RotReceiverImpl(CoteReceiver rec, CoinTossing ct) {
    super(rec, ct);
    this.receiver = rec;
  }

  @Override
  public List<StrictBitVector> extend(StrictBitVector choices) {
    int ellPrime = choices.getSize() + getKbitLength()
        + getLambdaSecurityParam();
    // Extend the choices with random choices for padding
    StrictBitVector paddingChoices = new StrictBitVector(
        getKbitLength() + getLambdaSecurityParam(), getRand());
    StrictBitVector extendedChoices = StrictBitVector.concat(choices,
        paddingChoices);
    // Use the choices along with the random padding uses for correlated OT with
    // errors
    List<StrictBitVector> tlist = receiver.extend(extendedChoices);
    // Agree on challenges for linear combination test
    List<StrictBitVector> chiList = getChallenges(ellPrime);
    StrictBitVector xvec = computeBitLinearCombination(extendedChoices, chiList);
    getNetwork().send(getOtherId(), xvec.toByteArray());
    StrictBitVector tvec = computeInnerProduct(chiList, tlist);
    getNetwork().send(getOtherId(), tvec.toByteArray());
    // Remove the correlation of the OTs by hashing
    List<StrictBitVector> vvec = hashBitVector(tlist, choices.getSize());
    return vvec;
  }

  /**
   * Computes the sum of element in a list based on a vector if indicator
   * variables. The sum will be based on Galois addition in the binary extension
   * field of the individual elements of the list. That is, through an XOR
   * operation. <br/>
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
