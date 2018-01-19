package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementUtils;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A helper class for multiplication-based protocols {@link dk.alexandra.fresco.tools.mascot.cope.CopeSigner}
 * and the multiplication sub-protocol used by {@link dk.alexandra.fresco.tools.mascot.triple.TripleGeneration}.
 * These two classes share a lot functionality. This functionality is implemented here.
 */
public class MultiplyLeftHelper {

  private final RotBatch rot;
  private final MascotResourcePool resourcePool;
  private final FieldElementUtils fieldElementUtils;

  public MultiplyLeftHelper(MascotResourcePool resourcePool, Network network, int otherId) {
    this.resourcePool = resourcePool;
    this.fieldElementUtils = new FieldElementUtils(resourcePool.getModulus());
    this.rot = resourcePool.createRot(otherId, network);
  }

  /**
   * Uses left factors as choice bits to receive seeds to prgs.
   *
   * @param leftFactors the left side of the multiplication
   * @param seedLength the length of the seeds that the ROT produces
   * @return list of seeds to prgs
   */
  public List<StrictBitVector> generateSeeds(List<FieldElement> leftFactors, int seedLength) {
    StrictBitVector packedFactors = getFieldElementUtils().pack(leftFactors);
    // use rot to get choice seeds
    List<StrictBitVector> seeds = getRot().receive(packedFactors, seedLength);
    Collections.reverse(seeds);
    return seeds;
  }

  public List<StrictBitVector> generateSeeds(FieldElement leftFactor, int seedLength) {
    return generateSeeds(Collections.singletonList(leftFactor), seedLength);
  }

  /**
   * Computes this party's shares of the products. <br> There is a product share per left factor.
   *
   * @param leftFactors this party's multiplication factors
   * @param feSeeds seeds as field elements
   * @param diffs the diffs received from other party
   * @return product shares
   */
  public List<FieldElement> computeProductShares(List<FieldElement> leftFactors,
      List<FieldElement> feSeeds, List<FieldElement> diffs) {
    List<FieldElement> result = new ArrayList<>(leftFactors.size());
    int diffIdx = 0;
    for (FieldElement leftFactor : leftFactors) {
      List<FieldElement> summands = new ArrayList<>(getResourcePool().getModBitLength());
      for (int b = 0; b < getResourcePool().getModBitLength(); b++) {
        FieldElement feSeed = feSeeds.get(diffIdx);
        FieldElement diff = diffs.get(diffIdx);
        boolean bit = leftFactor.getBit(b);
        FieldElement summand = diff.select(bit).add(feSeed);
        summands.add(summand);
        diffIdx++;
      }
      FieldElement productShare = getFieldElementUtils().recombine(summands);
      result.add(productShare);
    }
    return result;
  }

  private RotBatch getRot() {
    return rot;
  }

  private FieldElementUtils getFieldElementUtils() {
    return fieldElementUtils;
  }

  private MascotResourcePool getResourcePool() {
    return resourcePool;
  }
}
