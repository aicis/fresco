package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A helper class for multiplication-based protocols {@link dk.alexandra.fresco.tools.mascot.cope.CopeSigner}
 * and {@link MultiplyLeft}. These two classes share a lot functionality. This functionality is
 * implemented here.
 */
public class MultiplyLeftHelper extends MultiplySharedHelper {

  public MultiplyLeftHelper(MascotResourcePool resourcePool,
      Network network, Integer otherId, int numLeftFactors) {
    super(resourcePool, network, otherId, numLeftFactors);
  }

  public MultiplyLeftHelper(MascotResourcePool resourcePool,
      Network network, Integer otherId) {
    super(resourcePool, network, otherId, 1);
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
    // TODO temporary fix until big-endianness issue is resolved
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
    List<FieldElement> result = new ArrayList<FieldElement>(leftFactors.size());
    int diffIdx = 0;
    for (FieldElement leftFactor : leftFactors) {
      List<FieldElement> summands = new ArrayList<FieldElement>(getModBitLength());
      for (int b = 0; b < getModBitLength(); b++) {
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
}
