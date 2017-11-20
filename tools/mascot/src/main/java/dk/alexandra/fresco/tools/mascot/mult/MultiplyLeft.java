package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class MultiplyLeft extends MultiplyShared {

  public MultiplyLeft(Integer myId, Integer otherId, int kBitLength, int lambdaSecurityParam,
      int numLeftFactors, Random rand, ExtendedNetwork network, ExecutorService executor,
      BigInteger modulus) {
    super(myId, otherId, kBitLength, lambdaSecurityParam, numLeftFactors, rand, network, executor,
        modulus);
  }

  protected List<BigInteger> generateSeeds(List<FieldElement> leftFactors) {
    // TODO: the ROTs should be batched into one
    List<BigInteger> seeds = new ArrayList<>();
    for (FieldElement factor : leftFactors) {
      List<BigInteger> temp = rot.receive(factor.toBigInteger(), kBitLength);
      seeds.addAll(temp);
    }
    return seeds;
  }

  protected List<FieldElement> receiveDiffs(int numDiffs) {
    // TODO: need batch-receive
    List<FieldElement> diffs = new ArrayList<>();
    for (int d = 0; d < numDiffs; d++) {
      diffs.add(new FieldElement(network.receive(otherId), modulus, kBitLength));
    }
    return diffs;
  }

  public List<FieldElement> multiply(List<FieldElement> leftFactors) {
    List<BigInteger> seeds = generateSeeds(leftFactors);
    List<FieldElement> seedElements = seeds.stream()
        .map(seed -> new FieldElement(seed, modulus, kBitLength)).collect(Collectors.toList());
    List<FieldElement> diffs = receiveDiffs(seeds.size());
    // TODO: clean up
    List<FieldElement> productShares = new ArrayList<>();
    int absIdx = 0;
    for (FieldElement leftFactor : leftFactors) {
      List<FieldElement> qValues = new ArrayList<>();
      for (int k = 0; k < kBitLength; k++) {
        FieldElement seedElement = seedElements.get(absIdx);
        boolean bit = leftFactor.getBit(k);
        FieldElement qValue = diffs.get(absIdx).select(bit).add(seedElement);
        qValues.add(qValue);
        absIdx++;
      }
      productShares.add(FieldElement.recombine(qValues, modulus, kBitLength));
    }
    return productShares;
  }

}
