package dk.alexandra.fresco.tools.ot.otextension;

import java.util.List;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;

public class RotReceiver extends RotShared {

  private CoteReceiver receiver;

  public RotReceiver(CoteReceiver rec) {
    super(rec);
    this.receiver = rec;
  }

  public void initialize()
      throws FailedOtExtensionException, MaliciousCommitmentException,
      FailedCommitmentException, FailedCoinTossingException {
    receiver.initialize();
    ct.initialize();
    initialized = true;
  }

  public List<StrictBitVector> extend(StrictBitVector choices)
      throws FailedOtExtensionException {
    int ellPrime = choices.getSize() + getKbitLength()
        + getLambdaSecurityParam();
    // Extend the choices with random choices for padding
    StrictBitVector paddingChoices = new StrictBitVector(
        getKbitLength() + getLambdaSecurityParam(), getRand());
    StrictBitVector extendedChoices = StrictBitVector.concat(choices,
        paddingChoices);
    List<StrictBitVector> tvec = receiver.extend(extendedChoices);
    List<StrictBitVector> chiVec = getChallenges(ellPrime);
    StrictBitVector xval = computeBitLinearCombination(extendedChoices, chiVec);
    getNetwork().send(getOtherId(), xval.toByteArray());
    StrictBitVector tval = computeInnerProduct(chiVec, tvec);
    getNetwork().send(getOtherId(), tval.toByteArray());
    List<StrictBitVector> vvec = hashBitVector(tvec, choices.getSize());
    return vvec;
  }

  /**
   * Computes the sum of element in a list based on a vector if indicator
   * variables. The sum will be based on Galois addition in the binary extension
   * field of the individual elements of the list. That is, through an XOR
   * operation.
   * <p>
   * All elements of the list MUST have equal size! And both the list and vector
   * of indicator bits MUST contain an equal amount of entries!
   * </p>
   * 
   * @param indicators
   *          The vector of indicator bits
   * @param list
   *          The input list, with all elements of equal size
   * @return The inner product represented as a StrictBitVector
   */
  protected StrictBitVector computeBitLinearCombination(StrictBitVector indicators,
      List<StrictBitVector> list) {
    StrictBitVector res = new StrictBitVector(list.get(0).getSize());
    for (int i = 0; i < indicators.getSize(); i++) {
      if (indicators.getBit(i, false) == true) {
        res.xor(list.get(i));
      }
    }
    return res;
  }

}
