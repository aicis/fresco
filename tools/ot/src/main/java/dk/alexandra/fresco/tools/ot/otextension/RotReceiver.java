package dk.alexandra.fresco.tools.ot.otextension;

import java.util.List;

import dk.alexandra.fresco.framework.util.StrictBitVector;

/**
 * Protocol class for the party acting as the receiver in an random OT extension
 * following the Bristol 2015 OT extension.
 * 
 * @author jot2re
 *
 */
public class RotReceiver extends RotShared {

  private CoteReceiver receiver;

  /**
   * Construct a receiving party for an instance of the random OT extension
   * protocol.
   * 
   * @param rec
   *          The correlated OT with error receiver this protocol will use
   */
  public RotReceiver(CoteReceiver rec) {
    super(rec);
    this.receiver = rec;
  }

  /**
   * Initializes the random OT extension by initializing the underlying
   * correlated OT with errors and coin tossing functionalities. This should
   * only be done once for a given sender/receiver pair.
   */
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    if (receiver.initialized == false) {
      receiver.initialize();
    }
    ct.initialize();
    initialized = true;
  }

  /**
   * Constructs a new batch of random OTs.
   * 
   * @param choices
   *          The receivers choices for this extension. This MUST have size
   *          8*2^x-kbitLength-getLambdaSecurityParam for some x >=0.
   * @return A list of pairs consisting of the bit choices, followed by the
   *         received messages
   */
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