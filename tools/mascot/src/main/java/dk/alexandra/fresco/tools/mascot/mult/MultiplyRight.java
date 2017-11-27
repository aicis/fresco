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

  List<Pair<StrictBitVector, StrictBitVector>> generateSeeds(int numMults) {
    // perform rots for each bit, for each left factor, for each multiplication
    int numRots = ctx.getkBitLength() * numLeftFactors * numMults;
    List<Pair<StrictBitVector, StrictBitVector>> seeds = rot.send(numRots);
    return seeds;
  }

  FieldElement computeDiff(Pair<FieldElement, FieldElement> feSeedPair, FieldElement factor) {
    FieldElement left = feSeedPair.getFirst();
    FieldElement right = feSeedPair.getSecond();
    FieldElement diff = left.subtract(right).add(factor);
    return diff;
  }

  List<FieldElement> computeDiffs(List<Pair<FieldElement, FieldElement>> feSeedPairs,
      List<FieldElement> rightFactors) {
    List<FieldElement> diffs = new ArrayList<>(feSeedPairs.size());
    int modBitLength = ctx.getkBitLength();
    int rightFactorIdx = 0;
    int seedPairIdx = 0;
    for (Pair<FieldElement, FieldElement> feSeedPair : feSeedPairs) {
      FieldElement rightFactor = rightFactors.get(rightFactorIdx);
      FieldElement diff = computeDiff(feSeedPair, rightFactor);
      diffs.add(diff);
      seedPairIdx++;
      rightFactorIdx = seedPairIdx / (numLeftFactors * modBitLength);
    }
    return diffs;
  }

  void sendDiffs(List<FieldElement> diffs) {
    // TODO: need batch-send
    for (FieldElement diff : diffs) {
      ctx.getNetwork().send(otherId, diff.toByteArray());
    }
  }

  List<List<FieldElement>> computeProductShares(List<FieldElement> feZeroSeeds,
      int numRightFactors) {
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    int groupBitLength = numLeftFactors * modBitLength;
    List<List<FieldElement>> productShares = new ArrayList<>(numRightFactors);
    for (int rightFactIdx = 0; rightFactIdx < numRightFactors; rightFactIdx++) {
      List<FieldElement> resultGroup = new ArrayList<>(numLeftFactors);
      for (int leftFactIdx = 0; leftFactIdx < numLeftFactors; leftFactIdx++) {
        int from = rightFactIdx * groupBitLength + leftFactIdx * modBitLength;
        int to = rightFactIdx * groupBitLength + (leftFactIdx + 1) * modBitLength;
        List<FieldElement> subFactors = feZeroSeeds.subList(from, to);
        FieldElement recombined = FieldElement.recombine(subFactors, modulus, modBitLength);
        resultGroup.add(recombined.negate());
      }
      productShares.add(resultGroup);
    }
    return productShares;
  }

  public List<List<FieldElement>> multiply(List<FieldElement> rightFactors) {
    // we need the modulus and the bit length of the modulus
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();

    // generate seeds pairs which we will use to compute diffs
    List<Pair<StrictBitVector, StrictBitVector>> seedPairs = generateSeeds(rightFactors.size());
    
    // convert seeds pairs to field elements so we can compute on them
    List<Pair<FieldElement, FieldElement>> feSeedPairs = seedPairs.stream()
        .map(pair -> new Pair<>(
            new FieldElement(pair.getFirst().toByteArray(), modulus, modBitLength),
            new FieldElement(pair.getSecond().toByteArray(), modulus, modBitLength)))
        .collect(Collectors.toList());

    // compute q0 - q1 + b for each seed pair
    List<FieldElement> diffs = computeDiffs(feSeedPairs, rightFactors);

    // send diffs over to other party
    sendDiffs(diffs);

    // get zero index seeds
    List<FieldElement> feZeroSeeds =
        feSeedPairs.stream().map(feSeedPair -> feSeedPair.getFirst()).collect(Collectors.toList());

    // compute product shares
    List<List<FieldElement>> productShares = computeProductShares(feZeroSeeds, rightFactors.size());
    return productShares;
  }

}
