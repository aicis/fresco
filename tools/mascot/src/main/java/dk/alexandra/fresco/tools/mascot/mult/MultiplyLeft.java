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
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;

public class MultiplyLeft extends MultiplyShared {

  /**
   * Constructs one side of the two-party multiplication protocol.
   * 
   * @param resourcePool the resouce pool
   * @param network the network
   * @param otherId the other party's
   * @param numLeftFactors number of left factors per right factor
   */
  public MultiplyLeft(MascotResourcePool resourcePool, Network network, Integer otherId,
      int numLeftFactors) {
    super(resourcePool, network, otherId, numLeftFactors);
  }

  /**
   * {@link MultiplyLeft#MultiplyLeft(MascotResourcePool, Network, Integer, int)} with default left
   * factor number of 1.
   * 
   * @param resourcePool
   * @param network
   * @param otherId
   */
  public MultiplyLeft(MascotResourcePool resourcePool, Network network, Integer otherId) {
    this(resourcePool, network, otherId, 1);
  }

  /**
   * Uses left factors as choice bits to receive seeds to prgs.
   * 
   * @param leftFactors the left side of the multiplication
   * @param seedLength the length of the seeds that the ROT produces
   * @return list of seeds to prgs
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
   * {@link #generateSeeds}.
   * 
   * @param leftFactor
   * @param seedLength
   * @return
   */
  public List<StrictBitVector> generateSeeds(FieldElement leftFactor, int seedLength) {
    return generateSeeds(Collections.singletonList(leftFactor), seedLength);
  }

  /**
   * Receives diffs from other party.
   * 
   * @param numDiffs number of diffs to receive
   * @return field elements representing diffs
   */
  public List<FieldElement> receiveDiffs(int numDiffs) {
    byte[] raw = network.receive(otherId);
    List<FieldElement> diffs = getFieldElementSerializer().deserializeList(raw);
    return diffs;
  }

  /**
   * Computes this party's shares of the products. <br>
   * There is a product share per left factor.
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

  /**
   * Converts each seed to field element using the PRG.
   * 
   * @param seeds the seeds represented as bit vectors
   * @param modulus the modulus we are working in
   * @param modBitLength the bit length of the modulus
   * @return seeds converted to field elements via PRG
   */
  List<FieldElement> seedsToFieldElements(List<StrictBitVector> seeds, BigInteger modulus,
      int modBitLength) {
    // TODO there should be a better way to do this
    return seeds.stream()
        .map(seed -> {
          return new FieldElementPrgImpl(seed).getNext(modulus, modBitLength);
        })
        .collect(Collectors.toList());
  }

  /**
   * Computes shares of product of left factors and right factor held by other party. <br>
   * If this party holds l0, l1, l2 and other party holds r0, this will compute additive shares of
   * l0 * r0, l1 * r0, l2 * r0.
   * 
   * @param leftFactors this party's factors
   * @return shares of products
   */
  public List<FieldElement> multiply(List<FieldElement> leftFactors) {
    List<StrictBitVector> seeds = generateSeeds(leftFactors, getModBitLength());
    List<FieldElement> feSeeds = seedsToFieldElements(seeds, getModulus(), getModBitLength());
    List<FieldElement> diffs = receiveDiffs(seeds.size());
    return computeProductShares(leftFactors, feSeeds, diffs);
  }

}
