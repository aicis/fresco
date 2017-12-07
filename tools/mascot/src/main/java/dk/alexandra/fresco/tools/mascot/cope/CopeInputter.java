package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.FailedMultException;
import dk.alexandra.fresco.tools.mascot.mult.MaliciousMultException;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;
import dk.alexandra.fresco.tools.mascot.utils.DummyPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;

public class CopeInputter extends CopeShared {

  private List<FieldElementPrg> leftPrgs;
  private List<FieldElementPrg> rightPrgs;
  private MultiplyRight multiplier;

  public CopeInputter(MascotContext ctx, Integer otherId) {
    super(ctx, otherId);
    this.leftPrgs = new ArrayList<>();
    this.rightPrgs = new ArrayList<>();
    this.multiplier = new MultiplyRight(ctx, otherId);
  }

  public void initialize() throws MaliciousCopeException, FailedCopeException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    try {
      List<Pair<StrictBitVector, StrictBitVector>> seeds = multiplier.generateSeeds(1);
      seedPrgs(seeds);
      initialized = true;
    } catch (MaliciousMultException e) {
      throw new MaliciousCopeException("Malicious failure during initialization", e);
    } catch (FailedMultException e) {
      throw new FailedCopeException("Non-malicious failure during initialization", e);
    }
  }

  private void seedPrgs(List<Pair<StrictBitVector, StrictBitVector>> seeds) {
    for (Pair<StrictBitVector, StrictBitVector> seedPair : seeds) {
      this.leftPrgs.add(new DummyPrg(seedPair.getFirst(), modulus, modBitLength));
      this.rightPrgs.add(new DummyPrg(seedPair.getSecond(), modulus, modBitLength));
    }
  }

  List<Pair<FieldElement, FieldElement>> generateMaskPairs(BigInteger modulus, int modBitLength) {
    Stream<Pair<FieldElement, FieldElement>> maskStream = IntStream.range(0, leftPrgs.size())
        .mapToObj(idx -> {
          FieldElement t0 = this.leftPrgs.get(idx)
              .getNext(modulus, modBitLength);
          FieldElement t1 = this.rightPrgs.get(idx)
              .getNext(modulus, modBitLength);
          return new Pair<>(t0, t1);
        });
    return maskStream.collect(Collectors.toList());
  }

  List<Pair<FieldElement, FieldElement>> generateMaskPairs(int numInputs) {
    // for each input pair, we use our prf to get the next set of masks
    List<Pair<FieldElement, FieldElement>> maskPairs = new ArrayList<>();
    for (int i = 0; i < numInputs; i++) {
      // generate masks for single input
      maskPairs.addAll(generateMaskPairs(modulus, modBitLength));
    }
    return maskPairs;
  }

  public List<FieldElement> extend(List<FieldElement> inputElements) {
    // can't extend before we have set up the seeds
    if (!initialized) {
      throw new IllegalStateException("Cannot call extend before initializing");
    }

    // use seeds to generate mask pairs
    List<Pair<FieldElement, FieldElement>> maskPairs = generateMaskPairs(inputElements.size());

    // compute q0 - q1 + x for each input x for each mask pair
    List<FieldElement> diffs = multiplier.computeDiffs(maskPairs, inputElements);

    // send diffs
    multiplier.sendDiffs(diffs);

    // get zero index masks
    List<FieldElement> feZeroSeeds = maskPairs.stream()
        .map(feSeedPair -> feSeedPair.getFirst())
        .collect(Collectors.toList());

    // compute product shares
    List<FieldElement> productShares =
        multiplier.computeProductShares(feZeroSeeds, inputElements.size());

    return productShares;
  }

}
