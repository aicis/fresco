package dk.alexandra.fresco.tools.ot.otextension;

import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;

public class RotShared {
  // Internal state variables
  protected CoteShared cote;
  protected CoinTossing ct;

  RotShared(CoteShared cote) {
    super();
    if (cote.kbitLength < 1 || cote.lambdaSecurityParam < 1
        || cote.rand == null | cote.network == null) {
      throw new IllegalArgumentException("Illegal constructor parameters");
    }
    if (cote.kbitLength % 8 != 0) {
      throw new IllegalArgumentException(
          "Computational security parameter must be divisible by 8");
    }
    this.cote = cote;
    this.ct = new CoinTossing(cote.myId, cote.otherId, cote.kbitLength,
        cote.rand, cote.network);
  }

  public int getOtherId() {
    return cote.getOtherId();
  }

  public void setOtherId(int otherId) {
    cote.setOtherId(otherId);
  }

  public int getKbitLength() {
    return cote.getkBitLength();
  }

  public void setKbitLength(int kbitLength) {
    cote.setkBitLength(kbitLength);
  }

  public int getLambdaSecurityParam() {
    return cote.getLambdaSecurityParam();
  }

  public void setLambdaSecurityParam(int lambdaSecurityParam) {
    cote.setLambdaSecurityParam(lambdaSecurityParam);
  }

  public Random getRand() {
    return cote.getRand();
  }

  public void setRand(Random rand) {
    cote.setRand(rand);
  }

  public Network getNetwork() {
    return cote.getNetwork();
  }

  public void setNetwork(Network network) {
    cote.setNetwork(network);
  }

  protected StrictBitVector computePolyLinearCombination(
      List<StrictBitVector> chiVec, List<StrictBitVector> tvec) {
    StrictBitVector res = new StrictBitVector(2 * cote.kbitLength);
    for (int i = 0; i < chiVec.size(); i++) {
      StrictBitVector temp = multiplyWithoutReduction(chiVec.get(i),
          tvec.get(i));
      res.xor(temp);
    }
    return res;
  }

  protected StrictBitVector multiplyWithoutReduction(StrictBitVector a,
      StrictBitVector b) {
    StrictBitVector res = new StrictBitVector(a.getSize() + b.getSize());
    for (int i = 0; i < a.getSize(); i++) {
      // Note that this is not constant time!
      if (a.getBit(i) == true) {
        StrictBitVector temp = shiftArray(b, i, a.getSize() + b.getSize());
        res.xor(temp);
      }
    }
    return res;
  }

  private StrictBitVector shiftArray(StrictBitVector in, int pos, int maxSize) {
    if (in.getSize() + pos < maxSize) {
      throw new IllegalArgumentException(
          "The new vector is too small for the shift");
    }
    StrictBitVector res = new StrictBitVector(maxSize);
    for (int i = 0; i < in.getSize(); i++) {
      res.setBit(i + pos, in.getBit(i));
    }
    return res;
  }
}