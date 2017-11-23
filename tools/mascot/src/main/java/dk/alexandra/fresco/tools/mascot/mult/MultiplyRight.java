package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class MultiplyRight extends MultiplyShared {

  public MultiplyRight(MascotContext ctx, Integer otherId, int numLeftFactors) {
    super(ctx, otherId, numLeftFactors);
  }

  protected List<Pair<StrictBitVector, StrictBitVector>> generateSeeds() {
    // TODO: the ROTs should be batched into one
    List<Pair<StrictBitVector, StrictBitVector>> seeds = new ArrayList<>();
    for (int r = 0; r < numLeftFactors; r++) {
      seeds.addAll(rot.send(ctx.getkBitLength()));
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

  protected void sendDiffs(List<FieldElement> diffs) {
    // TODO: need batch-send
    for (FieldElement diff : diffs) {
      ctx.getNetwork().send(otherId, diff.toByteArray());
    }
  }

  public List<FieldElement> multiply(FieldElement rightFactor) {
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    // TODO: clean up
    List<Pair<StrictBitVector, StrictBitVector>> seeds = generateSeeds();
    // TODO: could do diffs in one pass
    List<Pair<FieldElement, FieldElement>> qValues = seeds.stream()
        .map(pair -> new Pair<>(
            new FieldElement(pair.getFirst().toByteArray(), modulus, modBitLength),
            new FieldElement(pair.getSecond().toByteArray(), modulus, modBitLength)))
        .collect(Collectors.toList());
    List<FieldElement> diffs = computeDiffs(qValues, rightFactor);
    sendDiffs(diffs);
    List<FieldElement> zeroSeeds =
        qValues.stream().map(seedPair -> seedPair.getFirst()).collect(Collectors.toList());
    List<FieldElement> productShares = new ArrayList<>();
    for (int i = 0; i < numLeftFactors; i++) {
      List<FieldElement> subFactors = zeroSeeds.subList(i * modBitLength, (i + 1) * modBitLength);
      FieldElement recombined = FieldElement.recombine(subFactors, modulus, modBitLength);
      productShares.add(recombined.negate());
    }
    return productShares;
  }

}
