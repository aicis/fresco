package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.security.MessageDigest;
import java.util.List;

/**
 * Protocol class for the party acting as the sender in a random OT extension.
 * 
 * @author jot2re
 *
 */
public class RotSender extends RotShared {

  private final MessageDigest digest;
  // The correlated OT with errors sender that this object will use
  private final CoteSender sender;

  /**
   * Construct a sending party for an instance of the random OT extension
   * protocol.
   * 
   * @param snd
   *          The correlated OT with error sender this protocol will use
   */
  public RotSender(CoteSender snd) {
    super(snd);
    sender = snd;

    this.digest = ExceptionConverter.safe(
        () -> MessageDigest.getInstance("SHA-256"),
        "Cannot load hashing algorithm");
  }

  /**
   * Initialize the random OT extension. This must only be done once.
   * 
   * @throws MaliciousCommitmentException
   *           Thrown if cheating occurs in commitments during initialization
   * @throws FailedCommitmentException
   *           Thrown if something, non-malicious, goes wrong in the commitments
   *           during initialization
   * @throws FailedCoinTossingException
   *           Thrown if something, non-malicious, goes wrong in the
   *           coin-tossing protocol during initialization
   * @throws FailedOtExtensionException
   *           Thrown if something, non-malicious, goes wrong in the
   *           initialization of the underlying correlated OT during
   *           initialization
   * @throws MaliciousOtExtensionException
   *           Thrown if cheating occurred
   */
  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    if (sender.initialized == false) {
      sender.initialize();
    }
    ct.initialize();
    initialized = true;
  }

  /**
   * Constructs a new batch of random OTs.
   * 
   * @param size
   *          The amount of random OTs to construct
   * @return A pair of lists of StrictBitVectors. First list consists of the
   *         choice-zero messages. Second list consists of the choice-one
   *         messages
   * @throws MaliciousOtExtensionException
   *           Thrown if cheating occurs
   * @throws FailedOtExtensionException
   *           Thrown if something, non-malicious, goes wrong
   */
  public Pair<List<StrictBitVector>, List<StrictBitVector>> extend(int size) {
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    int ellPrime = size + getKbitLength() + getLambdaSecurityParam();
    // Construct a sufficient amount correlated OTs with errors
    List<StrictBitVector> qlist = sender.extend(ellPrime);
    // Agree on a random challenge for each of the correlated OTs with errors
    List<StrictBitVector> chiList = getChallenges(ellPrime);
    // Retrieve the correlation from the correlated OTs with errors
    StrictBitVector delta = sender.getDelta();
    // Compute the linear combination of the correlated OTs with errors and the
    // random challenges
    StrictBitVector qvec = computeInnerProduct(chiList, qlist);
    // Retrieve the receivers parts of the correlation check challenge
    byte[] xvecBytes = getNetwork().receive(getOtherId());
    byte[] tvecBytes = getNetwork().receive(getOtherId());
    StrictBitVector xvec = new StrictBitVector(xvecBytes, getKbitLength());
    StrictBitVector tvec = new StrictBitVector(tvecBytes, 2 * getKbitLength());
    // Compute the challenge vector based on the receivers send values
    StrictBitVector tvecToCompare = multiplyWithoutReduction(delta, xvec);
    tvecToCompare.xor(qvec);
    // Ensure that the receiver has been honest by verifying its challenge
    if (tvecToCompare.equals(tvec) == false) {
      throw new MaliciousException(
          "Correlation check failed for the sender in the random OT extension");
    }
    // Remove the correlated of the first "size" messages by hashing for
    // choice-zero
    List<StrictBitVector> vlistZero = hashBitVector(qlist, size, digest);
    // XOR the correlated into all the values from the underlying correlated OT
    // with error to compute the choice-one message
    for (int i = 0; i < size; i++) {
      qlist.get(i).xor(delta);
    }
    // Remove the correlated for the choice-one as well
    List<StrictBitVector> vlistOne = hashBitVector(qlist, size, digest);
    Pair<List<StrictBitVector>, List<StrictBitVector>> res =
        new Pair<List<StrictBitVector>, List<StrictBitVector>>(vlistZero, vlistOne);
    return res;
  }
}
