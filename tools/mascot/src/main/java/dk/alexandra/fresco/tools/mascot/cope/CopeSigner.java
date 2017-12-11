package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.PaddingPrg;

public class CopeSigner extends CopeShared {

  private List<FieldElementPrg> prgs;
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
    this.prgs = new ArrayList<>();
  }

  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
      List<StrictBitVector> seeds = multiplier.generateSeeds(macKeyShare);
      seedPrgs(seeds);
      initialized = true;
  }

  private void seedPrgs(List<StrictBitVector> seeds) {
    for (StrictBitVector seed : seeds) {
      prgs.add(new PaddingPrg(seed));
    }
  }

  List<FieldElement> generateMasks(int numInputs, BigInteger modulus, int modBitLength) {
    // for each input pair, we use our prgs to get the next set of masks
    List<FieldElement> masks = new ArrayList<>();
    // generate mask for each input
    for (int i = 0; i < numInputs; i++) {
      // generate masks for single input
      List<FieldElement> singleInputMasks = prgs.stream()
          .map(prg -> prg.getNext(modulus, modBitLength))
          .collect(Collectors.toList());
      masks.addAll(singleInputMasks);
    }
    return masks;
  }

  public List<FieldElement> extend(int numInputs) {
    // can't extend before we have set up the seeds
    if (!initialized) {
      throw new IllegalStateException("Cannot call extend before initializing");
    }

    // compute chosen masks
    List<FieldElement> chosenMasks = generateMasks(numInputs, modulus, modBitLength);

    // get diffs from other party
    List<FieldElement> diffs = multiplier.receiveDiffs(numInputs * prgs.size());

    // use mac share for each input
    List<FieldElement> macKeyShares = IntStream.range(0, numInputs)
        .mapToObj(idx -> macKeyShare)
        .collect(Collectors.toList());

    // compute product shares
    List<FieldElement> productShares =
        multiplier.computeProductShares(macKeyShares, chosenMasks, diffs);

    return productShares;
  }

}
