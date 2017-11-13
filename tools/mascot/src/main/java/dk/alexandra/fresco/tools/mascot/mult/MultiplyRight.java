package dk.alexandra.fresco.tools.mascot.mult;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class MultiplyRight extends MultiplyShared {

  public MultiplyRight(Integer myId, Integer otherId, int kBitLength, int lambdaSecurityParam,
      int numLeftFactors, Random rand, ExtendedNetwork network, ExecutorService executor,
      BigInteger modulus) {
    super(myId, otherId, kBitLength, lambdaSecurityParam, numLeftFactors, rand, network, executor,
        modulus);
  }

  protected List<Pair<BigInteger, BigInteger>> generateSeeds() {
    // TODO: the ROTs should be batched into one
    List<Pair<BigInteger, BigInteger>> seeds = new ArrayList<>();
    for (int r = 0; r < numLeftFactors; r++) {
      seeds.addAll(rot.send(kBitLength));
    }
    return seeds;
  }

  protected List<FieldElement> computeDiffs(List<Pair<FieldElement, FieldElement>> qValues,
      FieldElement rightFactor) {
    List<FieldElement> diffs = qValues.stream().map((qPair) -> {
      return qPair.getFirst().subtract(qPair.getSecond()).add(rightFactor);
    }).collect(Collectors.toList());
    return diffs;
  }

  protected void sendDiffs(List<FieldElement> diffs) throws IOException {
    // TODO: need batch-send
    for (FieldElement diff : diffs) {
      network.send(0, otherId, diff.toByteArray());
    }
  }

  public List<FieldElement> multiply(FieldElement rightFactor) throws IOException {
    // TODO: clean up
    List<Pair<BigInteger, BigInteger>> seeds = generateSeeds();
    // TODO: could do diffs in one pass
    List<Pair<FieldElement, FieldElement>> qValues =
        seeds.stream()
            .map(pair -> new Pair<>(new FieldElement(pair.getFirst(), modulus, kBitLength),
                new FieldElement(pair.getSecond(), modulus, kBitLength)))
            .collect(Collectors.toList());
    List<FieldElement> diffs = computeDiffs(qValues, rightFactor);
    sendDiffs(diffs);
    List<FieldElement> zeroSeeds =
        qValues.stream().map(seedPair -> seedPair.getFirst()).collect(Collectors.toList());
    List<FieldElement> productShares = new ArrayList<>();
    for (int i = 0; i < numLeftFactors; i++) {
      List<FieldElement> subFactors = zeroSeeds.subList(i * kBitLength, (i + 1) * kBitLength);
      FieldElement recombined = FieldElement.recombine(subFactors, modulus, kBitLength);
      productShares.add(recombined.negate());
    }
    return productShares;
  }

}
