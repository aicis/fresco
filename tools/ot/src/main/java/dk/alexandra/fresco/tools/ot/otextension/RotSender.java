package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;

public class RotSender extends RotShared {

  private CoteSender sender;

  public RotSender(CoteSender snd) {
    super(snd);
    sender = snd;
  }

  public void initialize()
      throws NoSuchAlgorithmException, MaliciousCommitmentException,
      FailedCommitmentException, FailedCoinTossingException {
    sender.initialize();
    ct.initialize();
  }

  public Pair<List<StrictBitVector>, List<StrictBitVector>> extend(int size)
      throws MaliciousOtExtensionException {
    int ellPrime = size + sender.kbitLength + sender.lambdaSecurityParam;
    if (ellPrime % 8 != 0) {
      throw new IllegalArgumentException(
          "The sum of the amount to extend and the security parameters must be a factor of 8.");
    }
    List<StrictBitVector> chiVec = new ArrayList<>(ellPrime);
    for (int i = 0; i < ellPrime; i++) {
      StrictBitVector currentChi = ct.toss(ellPrime);
      chiVec.add(currentChi);
    }
    List<StrictBitVector> qvec = sender.extend(size);
    StrictBitVector qval = computePolyLinearCombination(chiVec, qvec);
    byte[] xvalbytes = sender.network.receive(sender.otherId);
    byte[] tvalbytes = sender.network.receive(sender.otherId);
    StrictBitVector xval = new StrictBitVector(xvalbytes, sender.kbitLength);
    StrictBitVector tval = new StrictBitVector(tvalbytes, sender.kbitLength);
    StrictBitVector tprime = multiplyWithoutReduction(sender.getDelta(), xval);
    tprime.xor(qval);
    if (tprime.equals(tval) == false) {
      throw new MaliciousOtExtensionException("Correlation check failed");
    }
    List<StrictBitVector> vvecZero = new ArrayList<>(size);
    List<StrictBitVector> vvecOne = new ArrayList<>(size);
    Pair<List<StrictBitVector>, List<StrictBitVector>> res = new Pair<List<StrictBitVector>, List<StrictBitVector>>(
        vvecZero, vvecOne);

    return res;
  }
}
