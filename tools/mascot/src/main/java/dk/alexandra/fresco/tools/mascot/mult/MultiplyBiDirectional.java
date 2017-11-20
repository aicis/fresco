package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class MultiplyBiDirectional {

  protected MultiplyLeft left;
  protected MultiplyRight right;

  public MultiplyBiDirectional(Integer myId, Integer otherId, int kBitLength,
      int lambdaSecurityParam, int numLeftFactors, Random rand, ExtendedNetwork network,
      ExecutorService executor, BigInteger modulus) {
    left = new MultiplyLeft(myId, otherId, kBitLength, lambdaSecurityParam, numLeftFactors, rand,
        network, executor, modulus);
    right = new MultiplyRight(myId, otherId, kBitLength, lambdaSecurityParam, numLeftFactors, rand,
        network, executor, modulus);
  }

  public Integer getOtherId() {
    return left.getOtherId();
  }
  
  public List<FieldElement> multiplyLeft(List<FieldElement> leftFactors) {
    return left.multiply(leftFactors);
  }

  public List<FieldElement> multiplyRight(FieldElement rightFactor) {
    return right.multiply(rightFactor);
  }

}
