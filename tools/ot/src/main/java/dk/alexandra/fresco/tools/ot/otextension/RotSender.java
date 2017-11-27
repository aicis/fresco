package dk.alexandra.fresco.tools.ot.otextension;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
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
    initialized = true;
  }

  public Pair<List<StrictBitVector>, List<StrictBitVector>> extend(int size)
      throws MaliciousOtExtensionException, NoSuchAlgorithmException {
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    int ellPrime = size + getKbitLength() + getLambdaSecurityParam();
    List<StrictBitVector> chiVec = new ArrayList<>(ellPrime);
    for (int i = 0; i < ellPrime; i++) {
      StrictBitVector currentChi = ct.toss(getKbitLength());
      chiVec.add(currentChi);
    }
    List<StrictBitVector> qvec = sender.extend(ellPrime);
    StrictBitVector qval = computePolyLinearCombination(chiVec, qvec);
    byte[] xvalbytes = sender.network.receive(sender.otherId);
    byte[] tvalbytes = sender.network.receive(sender.otherId);
    StrictBitVector xval = new StrictBitVector(xvalbytes, sender.kbitLength);
    StrictBitVector tval = new StrictBitVector(tvalbytes,
        2 * sender.kbitLength);
    StrictBitVector tprime = multiplyWithoutReduction(sender.getDelta(), xval);
    tprime.xor(qval);
    if (tprime.equals(tval) == false) {
      throw new MaliciousOtExtensionException("Correlation check failed");
    }
    List<StrictBitVector> vvecZero = new ArrayList<>(size);
    List<StrictBitVector> vvecOne = new ArrayList<>(size);
    Pair<List<StrictBitVector>, List<StrictBitVector>> res = new Pair<List<StrictBitVector>, List<StrictBitVector>>(
        vvecZero, vvecOne);
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    // Size of an int is always 4 bytes in java
    ByteBuffer indexBuffer = ByteBuffer.allocate(4 + (getKbitLength() / 8));
    byte[] hash;
    for (int i = 0; i < size; i++) {
      indexBuffer.clear();
      indexBuffer.putInt(i);
      indexBuffer.put(qvec.get(i).toByteArray());
      hash = digest.digest(indexBuffer.array());
      vvecZero.add(new StrictBitVector(hash, 256));
      indexBuffer.clear();
      indexBuffer.putInt(i);
      qvec.get(i).xor(sender.getDelta());
      indexBuffer.put(qvec.get(i).toByteArray());
      hash = digest.digest(indexBuffer.array());
      vvecOne.add(new StrictBitVector(hash, 256));
    }
    return res;
  }
}
