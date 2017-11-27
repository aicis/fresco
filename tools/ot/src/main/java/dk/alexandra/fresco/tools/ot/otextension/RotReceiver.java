package dk.alexandra.fresco.tools.ot.otextension;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
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
    initialized = true;
  }

  public List<StrictBitVector> extend(StrictBitVector choices)
      throws NoSuchAlgorithmException {
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    int ellPrime = choices.getSize() + getKbitLength()
        + getLambdaSecurityParam();
    // Extend the choices with random choices for padding
    StrictBitVector paddingChoices = new StrictBitVector(
        getKbitLength() + getLambdaSecurityParam(), getRand());

    StrictBitVector extendedChoices = StrictBitVector.concat(choices,
        paddingChoices);
    List<StrictBitVector> chiVec = new ArrayList<>(ellPrime);
    for (int i = 0; i < ellPrime; i++) {
      StrictBitVector currentChi = ct.toss(getKbitLength());
      chiVec.add(currentChi);
    }
    List<StrictBitVector> tvec = receiver.extend(extendedChoices);
    StrictBitVector xval = computeBitLinearCombination(extendedChoices, chiVec);
    StrictBitVector tval = computePolyLinearCombination(chiVec, tvec);
    receiver.network.send(receiver.otherId, xval.toByteArray());
    receiver.network.send(receiver.otherId, tval.toByteArray());
    List<StrictBitVector> vvec = new ArrayList<>(choices.getSize());
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    // Size of an int is always 4 bytes in java
    ByteBuffer indexBuffer = ByteBuffer.allocate(4 + (getKbitLength() / 8));
    byte[] hash;
    for (int i = 0; i < choices.getSize(); i++) {
      indexBuffer.clear();
      indexBuffer.putInt(i);
      indexBuffer.put(tvec.get(i).toByteArray());
      hash = digest.digest(indexBuffer.array());
      vvec.add(new StrictBitVector(hash, 256));
    }
    return vvec;
  }

  protected StrictBitVector computeBitLinearCombination(StrictBitVector xvec,
      List<StrictBitVector> chiVec) {
    StrictBitVector res = new StrictBitVector(getKbitLength());
    for (int i = 0; i < xvec.getSize(); i++) {
      if (xvec.getBit(i, false) == true) {
        res.xor(chiVec.get(i));
      }
    }
    return res;
  }

}
