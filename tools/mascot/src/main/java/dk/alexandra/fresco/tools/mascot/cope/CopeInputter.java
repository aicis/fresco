package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;

public class CopeInputter extends CopeShared {

  private List<Pair<StrictBitVector, StrictBitVector>> seeds;
  private MultiplyRight multiplier;

  public CopeInputter(MascotContext ctx, Integer otherId) {
    super(ctx, otherId);
    this.seeds = new ArrayList<>();
    this.multiplier = new MultiplyRight(ctx, otherId);
  }

  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    seeds = multiplier.generateSeeds(1);
    initialized = true;
  }

  List<Pair<FieldElement, FieldElement>> generateMaskPairs(BigInteger prfCounter,
      BigInteger modulus, int modBitLength) {
    Stream<Pair<FieldElement, FieldElement>> maskStream = seeds.stream()
        .map((seedPair) -> {
          FieldElement t0 =
              this.prf.evaluate(seedPair.getFirst(), prfCounter, modulus, modBitLength);
          FieldElement t1 =
              this.prf.evaluate(seedPair.getSecond(), prfCounter, modulus, modBitLength);
          return new Pair<>(t0, t1);
        });
    return maskStream.collect(Collectors.toList());
  }

  List<Pair<FieldElement, FieldElement>> generateMaskPairs(int numInputs) {
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    // for each input pair, we use our prf to get the next set of masks
    // each input requires a counter increment
    List<Pair<FieldElement, FieldElement>> maskPairs = new ArrayList<>();
    for (int i = 0; i < numInputs; i++) {
      // generate masks for single input
      maskPairs.addAll(generateMaskPairs(prfCounter, modulus, modBitLength));
      // increment prf counter
      prfCounter = prfCounter.add(BigInteger.ONE);
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
    List<List<FieldElement>> wrappedProductShares =
        multiplier.computeProductShares(feZeroSeeds, inputElements.size());
    
    // return unwrapped product shares
    return wrappedProductShares.stream()
        .flatMap(l -> l.stream())
        .collect(Collectors.toList());
  }

}
