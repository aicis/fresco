package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;

public class CopeSigner extends CopeShared {

  private List<StrictBitVector> chosenSeeds;
  private FieldElement macKeyShare;
  private MultiplyLeft multiplier;

  /**
   * 
   * @param ctx
   * @param otherId
   * @param macKeyShare
   */
  public CopeSigner(MascotContext ctx, Integer otherId, FieldElement macKeyShare) {
    super(ctx, otherId);
    this.macKeyShare = macKeyShare;
    this.multiplier = new MultiplyLeft(ctx, otherId);
    this.chosenSeeds = new ArrayList<>();
  }

  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    chosenSeeds = multiplier.generateSeeds(macKeyShare);
    initialized = true;
  }

  List<FieldElement> generateMasks(int numInputs) {
    BigInteger modulus = ctx.getModulus();
    int modBitLength = ctx.getkBitLength();
    // for each input pair, we use our prf to get the next set of masks
    // each input requires a counter increment
    List<FieldElement> masks = new ArrayList<>();
    for (int i = 0; i < numInputs; i++) {
      // generate masks for single input
      List<FieldElement> singleInputMasks = chosenSeeds.stream()
          .map(seed -> prf.evaluate(seed, prfCounter, modulus, modBitLength))
          .collect(Collectors.toList());
      masks.addAll(singleInputMasks);
      // increment prf counter
      prfCounter = prfCounter.add(BigInteger.ONE);
    }
    return masks;
  }

  public List<FieldElement> extend(int numInputs) {
    // can't extend before we have set up the seeds
    if (!initialized) {
      throw new IllegalStateException("Cannot call extend before initializing");
    }

    // compute chosen masks
    List<FieldElement> chosenMasks = generateMasks(numInputs);

    // get diffs from other party
    List<FieldElement> diffs = multiplier.receiveDiffs(numInputs * chosenSeeds.size());

    // use mac share for each input
    List<FieldElement> macKeyShares = IntStream.range(0, numInputs)
        .mapToObj(idx -> macKeyShare) // copy?
        .collect(Collectors.toList());
    
    // compute product shares
    List<List<FieldElement>> wrappedProductShares =
        multiplier.computeProductShares(Arrays.asList(macKeyShares), chosenMasks, diffs);
    
    // return unwrapped product shares
    return wrappedProductShares.stream()
        .flatMap(l -> l.stream())
        .collect(Collectors.toList());
  }

}
