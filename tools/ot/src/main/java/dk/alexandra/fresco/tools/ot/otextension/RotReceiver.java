package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
      throws NoSuchAlgorithmException, MaliciousCommitmentException,
      FailedCommitmentException, FailedCoinTossingException {
    receiver.initialize();
    ct.initialize();
  }

  public List<StrictBitVector> extend(StrictBitVector randomChoices) {
    int ellPrime = randomChoices.getSize() + receiver.kbitLength
        + receiver.lambdaSecurityParam;
    if (ellPrime % 8 != 0) {
      throw new IllegalArgumentException(
          "The sum of the amount to extend and the security parameters must be a factor of 8.");
    }
    List<StrictBitVector> chiVec = new ArrayList<>(ellPrime);
    for (int i = 0; i < ellPrime; i++) {
      StrictBitVector currentChi = ct.toss(ellPrime);
      chiVec.add(currentChi);
    }
    List<StrictBitVector> tvec = receiver.extend(randomChoices);
    StrictBitVector xval = computeBitLinearCombination(randomChoices, chiVec);
    StrictBitVector tval = computePolyLinearCombination(chiVec, tvec);
    receiver.network.send(receiver.otherId, xval.toByteArray());
    receiver.network.send(receiver.otherId, tval.toByteArray());

    List<StrictBitVector> vvec = new ArrayList<>(randomChoices.getSize());
    return vvec;
  }

  protected StrictBitVector computeBitLinearCombination(StrictBitVector xvec,
      List<StrictBitVector> chiVec) {
    StrictBitVector res = new StrictBitVector(receiver.kbitLength);
    for (int i = 0; i < xvec.getSize(); i++) {
      if (xvec.getBit(i) == true) {
        res.xor(chiVec.get(i));
      }
    }
    return res;
  }

}
