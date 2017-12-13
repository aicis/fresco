package dk.alexandra.fresco.tools.mascot.mult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementCollectionUtils;
import dk.alexandra.fresco.tools.mascot.utils.PaddingPrg;

public class MultiplyLeft extends MultiplyShared {

  public MultiplyLeft(MascotResourcePool resourcePool, Network network, Integer otherId, int numLeftFactors) {
    super(resourcePool, network, otherId, numLeftFactors);
  }

  public MultiplyLeft(MascotResourcePool resourcePool, Network network, Integer otherId) {
    this(resourcePool, network, otherId, 1);
  }
  
  /**
   * Uses left factors as choice bits to receive seeds to prgs.
   */
  public List<StrictBitVector> generateSeeds(List<FieldElement> leftFactors, int seedLength) {
    StrictBitVector packedFactors = FieldElementCollectionUtils.pack(leftFactors);
    // use rot to get choice seeds
    List<StrictBitVector> seeds = rot.receive(packedFactors, seedLength);
    // TODO temporary fix until big-endianness issue is resolved
    Collections.reverse(seeds);
    return seeds;
  }

  /**
   * {@link #generateSeeds}
   */
  public List<StrictBitVector> generateSeeds(FieldElement leftFactor, int seedLength) {
    return generateSeeds(Collections.singletonList(leftFactor), seedLength);
  }

  public List<FieldElement> receiveDiffs(int numDiffs) {
    byte[] raw = network.receive(otherId);
    List<FieldElement> diffs = getFieldElementSerializer().deserializeList(raw);
    return diffs;
  }

  public List<FieldElement> computeProductShares(List<FieldElement> leftFactors,
      List<FieldElement> feSeeds, List<FieldElement> diffs) {
    List<FieldElement> result = new ArrayList<>(leftFactors.size());
    int diffIdx = 0;
    for (FieldElement leftFactor : leftFactors) {
      List<FieldElement> summands = new ArrayList<>(getModBitLength());
      for (int b = 0; b < getModBitLength(); b++) {
        FieldElement feSeed = feSeeds.get(diffIdx);
        FieldElement diff = diffs.get(diffIdx);
        boolean bit = leftFactor.getBit(b);
        FieldElement summand = diff.select(bit)
            .add(feSeed);
        summands.add(summand);
        diffIdx++;
      }
      FieldElement productShare =
          FieldElementCollectionUtils.recombine(summands, getModulus(), getModBitLength());
      result.add(productShare);
    }
    return result;
  }

  List<FieldElement> seedsToFieldElements(List<StrictBitVector> seeds, BigInteger modulus,
      int modBitLength) {
    // TODO there should be a better way to do this
    return seeds.stream()
        .map(seed -> {
          return new PaddingPrg(seed).getNext(modulus, modBitLength);
        })
        .collect(Collectors.toList());
  }

  public List<FieldElement> multiply(List<FieldElement> leftFactors) {
    // generate seeds to use for multiplication
    List<StrictBitVector> seeds = generateSeeds(leftFactors, getModBitLength());

    // convert each seed to field element
    List<FieldElement> feSeeds = seedsToFieldElements(seeds, getModulus(), getModBitLength());

    // get diffs from other party
    List<FieldElement> diffs = receiveDiffs(seeds.size());

    // compute our shares of the product and return
    return computeProductShares(leftFactors, feSeeds, diffs);
  }

}
