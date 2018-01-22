package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.Collections;
import java.util.List;

public class RotSenderImpl extends RotSharedImpl implements RotSender {
  private final CoteSender sender;
  private final OtExtensionResourcePool resources;
  private final Network network;

  /**
   * Construct a sending party for an instance of the random OT extension protocol.
   *
   * @param snd
   *          The correlated OT with error sender this protocol will use
   * @param resources
   *          The common OT extension resources
   * @param network
   *          The network to use
   */
  public RotSenderImpl(CoteSender snd, OtExtensionResourcePool resources,
      Network network) {
    super(resources.getCoinTossing(), resources.getDigest(), resources
        .getComputationalSecurityParameter());
    this.sender = snd;
    this.resources = resources;
    this.network = network;
  }

  @Override
  public Pair<List<StrictBitVector>, List<StrictBitVector>> extend(int size) {
    int ellPrime = size + resources.getComputationalSecurityParameter()
        + resources.getLambdaSecurityParam();
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
    byte[] xvecBytes = network.receive(resources.getOtherId());
    byte[] tvecBytes = network.receive(resources.getOtherId());
    StrictBitVector xvec = new StrictBitVector(xvecBytes);
    StrictBitVector tvec = new StrictBitVector(tvecBytes);
    // Compute the challenge vector based on the receivers send values
    StrictBitVector tvecToCompare = computeInnerProduct(
        Collections.singletonList(delta),
        Collections.singletonList(xvec));
    tvecToCompare.xor(qvec);
    // Ensure that the receiver has been honest by verifying its challenge
    if (!tvecToCompare.equals(tvec)) {
      throw new MaliciousException(
          "Correlation check failed for the sender in the random OT extension");
    }
    // Remove the correlated of the first "size" messages by hashing for
    // choice-zero
    List<StrictBitVector> vlistZero = hashBitVector(qlist, size);
    // XOR the correlated into all the values from the underlying correlated OT
    // with error to compute the choice-one message
    for (int i = 0; i < size; i++) {
      qlist.get(i).xor(delta);
    }
    // Remove the correlated for the choice-one as well
    List<StrictBitVector> vlistOne = hashBitVector(qlist, size);
    Pair<List<StrictBitVector>, List<StrictBitVector>> res =
        new Pair<>(vlistZero, vlistOne);
    return res;
  }
}
