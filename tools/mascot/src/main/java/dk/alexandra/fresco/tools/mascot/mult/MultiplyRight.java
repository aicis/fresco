package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementCollectionUtils;
import dk.alexandra.fresco.tools.mascot.utils.DummyPrg;

public class MultiplyRight extends MultiplyShared {

  public MultiplyRight(MascotContext ctx, Integer otherId, int numLeftFactors) {
    super(ctx, otherId, numLeftFactors);
  }

  public MultiplyRight(MascotContext ctx, Integer otherId) {
    this(ctx, otherId, 1);
  }

  public List<Pair<StrictBitVector, StrictBitVector>> generateSeeds(int numMults) {
    // perform rots for each bit, for each left factor, for each multiplication
    int numRots = modBitLength * numLeftFactors * numMults;
    List<Pair<StrictBitVector, StrictBitVector>> seeds = new ArrayList<>();
    seeds = rot.send(numRots, modBitLength);
    // TODO temporary fix until big-endianness issue is resolved
    Collections.reverse(seeds);
    return seeds;
  }

  FieldElement computeDiff(Pair<FieldElement, FieldElement> feSeedPair, FieldElement factor) {
    FieldElement left = feSeedPair.getFirst();
    FieldElement right = feSeedPair.getSecond();
    FieldElement diff = left.subtract(right)
        .add(factor);
    return diff;
  }

  public List<FieldElement> computeDiffs(List<Pair<FieldElement, FieldElement>> feSeedPairs,
      List<FieldElement> rightFactors) {
    List<FieldElement> diffs = new ArrayList<>(feSeedPairs.size());
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

  public void sendDiffs(List<FieldElement> diffs) {
    network.send(otherId, feSerializer.serialize(diffs));
  }

  public List<FieldElement> computeProductShares(List<FieldElement> feZeroSeeds,
      int numRightFactors) {
    int groupBitLength = numLeftFactors * modBitLength;
    List<FieldElement> productShares = new ArrayList<>(numRightFactors);
    for (int rightFactIdx = 0; rightFactIdx < numRightFactors; rightFactIdx++) {
      for (int leftFactIdx = 0; leftFactIdx < numLeftFactors; leftFactIdx++) {
        int from = rightFactIdx * groupBitLength + leftFactIdx * modBitLength;
        int to = rightFactIdx * groupBitLength + (leftFactIdx + 1) * modBitLength;
        List<FieldElement> subFactors = feZeroSeeds.subList(from, to);
        FieldElement recombined =
            FieldElementCollectionUtils.recombine(subFactors, modulus, modBitLength);
        productShares.add(recombined.negate());
      }
    }
    return productShares;
  }

  List<Pair<FieldElement, FieldElement>> seedsToFieldElements(
      List<Pair<StrictBitVector, StrictBitVector>> seedPairs, BigInteger modulus,
      int modBitLength) {
    // TODO there should be a better way to do this
    return seedPairs.stream()
        .map(pair -> {
          FieldElement t0 =
              new DummyPrg(pair.getFirst(), modulus, modBitLength).getNext(modulus, modBitLength);
          FieldElement t1 =
              new DummyPrg(pair.getSecond(), modulus, modBitLength).getNext(modulus, modBitLength);
          return new Pair<>(t0, t1);
        })
        .collect(Collectors.toList());
  }

  public List<FieldElement> multiply(List<FieldElement> rightFactors) {
    // generate seeds pairs which we will use to compute diffs
    List<Pair<StrictBitVector, StrictBitVector>> seedPairs = generateSeeds(rightFactors.size());

    // convert seeds pairs to field elements so we can compute on them
    List<Pair<FieldElement, FieldElement>> feSeedPairs =
        seedsToFieldElements(seedPairs, modulus, modBitLength);

    // compute q0 - q1 + b for each seed pair
    List<FieldElement> diffs = computeDiffs(feSeedPairs, rightFactors);

    // send diffs over to other party
    sendDiffs(diffs);

    // get zero index seeds
    List<FieldElement> feZeroSeeds = feSeedPairs.stream()
        .map(Pair::getFirst)
        .collect(Collectors.toList());

    // compute product shares
    return computeProductShares(feZeroSeeds, rightFactors.size());
  }

}