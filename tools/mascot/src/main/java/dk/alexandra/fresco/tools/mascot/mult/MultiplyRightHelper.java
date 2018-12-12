package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementUtils;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A helper class for multiplication-based protocols {@link dk.alexandra.fresco.tools.mascot.cope.CopeInputter}
 * and the multiplication sub-protocol used by {@link dk.alexandra.fresco.tools.mascot.triple.TripleGeneration}.
 * These two classes share a lot of functionality. This functionality is implemented here.
 */
public class MultiplyRightHelper {

  private final RotBatch rot;
  private final MascotResourcePool resourcePool;
  private final FieldElementUtils fieldElementUtils;

  public MultiplyRightHelper(MascotResourcePool resourcePool, Network network, int otherId) {
    this.resourcePool = resourcePool;
    this.fieldElementUtils = new FieldElementUtils(resourcePool.getModulus());
    this.rot = resourcePool.createRot(otherId, network);
  }

  /**
   * Generate random seed pairs using OT.
   *
   * <p> The seed pairs are correlated with the multiplication factors of the other party. If the
   * other party's factors (represented as a bit vector) is 010, this party will receive seed pairs
   * <i>(a<sub>0</sub>, a<sub>1</sub>), (b<sub>0</sub>, b<sub>1</sub>), (c<sub>0</sub>,
   * c<sub>1</sub>)</i> whereas the other party will receive seeds <i>a<sub>0</sub>, b<sub>1</sub>,
   * c<sub>0</sub></i>. The parties can use the resulting seeds to compute the shares of the product
   * of their factors. </p>
   *
   * @param numMults the number of total multiplications
   * @param seedLength the bit length of the seeds
   * @return the seed pairs
   */
  public List<Pair<StrictBitVector, StrictBitVector>> generateSeeds(int numMults, int seedLength) {
    // perform rots for each bit, for each left factor, for each multiplication
    int numRots = resourcePool.getModBitLength() * numMults;
    List<Pair<StrictBitVector, StrictBitVector>> seeds = rot.send(numRots, seedLength);
    Collections.reverse(seeds);
    return seeds;
  }

  /**
   * Computes "masked" share of each bit of each of this party's factors.
   *
   * <p> For each seed pair <i>(q<sub>0</sub>, q<sub>1</sub>)<sub>n</sub></i> compute
   * <i>q<sub>0</sub> - q<sub>1</sub> + b<sub>n</sub></i> where <i>b<sub>n</sub></i> is the
   * <i>n</i>-th factor of this party's factor. </p>
   *
   * @param feSeedPairs seed pairs as field elements
   * @param rightFactors this party's factors
   * @return masked shares of this party's factor's bits.
   */
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
      rightFactorIdx = seedPairIdx / (resourcePool.getModBitLength());
    }
    return diffs;
  }

  /**
   * Computes this party's shares of the final products. <br> For each seed pair (q0, q1) this party
   * holds, uses q0 to recombine into field elements representing the product shares.
   *
   * @param feZeroSeeds the zero choice seeds
   * @param numRightFactors number of total right factors
   * @return shares of products
   */
  public List<FieldElement> computeProductShares(List<FieldElement> feZeroSeeds,
      int numRightFactors) {
    List<FieldElement> productShares = new ArrayList<>(numRightFactors);
    for (int rightFactIdx = 0; rightFactIdx < numRightFactors; rightFactIdx++) {
      int from = rightFactIdx * resourcePool.getModBitLength();
      int to = (rightFactIdx + 1) * resourcePool.getModBitLength();
      List<FieldElement> subFactors = feZeroSeeds.subList(from, to);
      FieldElement recombined = fieldElementUtils.recombine(subFactors);
      productShares.add(recombined.negate());
    }
    return productShares;
  }

  private FieldElement computeDiff(Pair<FieldElement, FieldElement> feSeedPair,
      FieldElement factor) {
    FieldElement left = feSeedPair.getFirst();
    FieldElement right = feSeedPair.getSecond();
    return left.subtract(right).add(factor);
  }

}
