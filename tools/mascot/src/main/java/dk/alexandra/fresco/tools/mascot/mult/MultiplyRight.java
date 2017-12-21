package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actively-secure two-party protocol for computing the product of two secret inputs. <br>
 * One input is held by the left party, the other by the right party. The protocol is asymmetric in
 * the sense that the left party performs a different computation from the right party. This class
 * implements the functionality of the right party. For the other side, see {@link MultiplyLeft}.
 * The resulting product is secret-shared among the two parties.
 */
public class MultiplyRight extends MultiplyShared {

  public MultiplyRight(MascotResourcePool resourcePool, Network network, Integer otherId,
      int numLeftFactors) {
    super(resourcePool, network, otherId, numLeftFactors);
  }

  public MultiplyRight(MascotResourcePool resourcePool, Network network, Integer otherId) {
    this(resourcePool, network, otherId, 1);
  }

  /**
   * Generate random seed pairs using OT. <br>
   * The seed pairs are correlated with the multiplication factors of the other party. If the other
   * party's factors (represented as a bit vector) is 010, this party will receive seed pairs (a0,
   * a1), (b0, b1), (c0, c1) whereas the other party will receive seeds a0, b1, c0. The parties can
   * use the resulting seeds to compute the shares of the product of their factors.
   * 
   * @param numMults the number of total multiplications
   * @param seedLength the bit length of the seeds
   * @return the seed pairs
   */
  public List<Pair<StrictBitVector, StrictBitVector>> generateSeeds(int numMults, int seedLength) {
    // perform rots for each bit, for each left factor, for each multiplication
    int numRots = getModBitLength() * getNumLeftFactors() * numMults;
    List<Pair<StrictBitVector, StrictBitVector>> seeds = getRot().send(numRots, seedLength);
    // TODO temporary fix until big-endianness issue is resolved
    Collections.reverse(seeds);
    return seeds;
  }

  FieldElement computeDiff(Pair<FieldElement, FieldElement> feSeedPair, FieldElement factor) {
    FieldElement left = feSeedPair.getFirst();
    FieldElement right = feSeedPair.getSecond();
    FieldElement diff = left.subtract(right).add(factor);
    return diff;
  }

  /**
   * Computes "masked" share of each bit of each of this party's factors. <br>
   * For each seed pair (q0, q1)_n compute q0 - q1 + bn where bn is the n-th bit of this party's
   * factor.
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
      rightFactorIdx = seedPairIdx / (getNumLeftFactors() * getModBitLength());
    }
    return diffs;
  }

  public void sendDiffs(List<FieldElement> diffs) {
    getNetwork().send(otherId, getFieldElementSerializer().serialize(diffs));
  }

  /**
   * Computes this party's shares of the final products. <br>
   * For each seed pair (q0, q1) this party holds, uses q0 to recombine into field elements
   * representing the product shares.
   * 
   * @param feZeroSeeds the zero choice seeds
   * @param numRightFactors number of total right factors
   * @return shares of products
   */
  public List<FieldElement> computeProductShares(List<FieldElement> feZeroSeeds,
      int numRightFactors) {
    int groupBitLength = getNumLeftFactors() * getModBitLength();
    List<FieldElement> productShares = new ArrayList<>(numRightFactors);
    for (int rightFactIdx = 0; rightFactIdx < numRightFactors; rightFactIdx++) {
      for (int leftFactIdx = 0; leftFactIdx < getNumLeftFactors(); leftFactIdx++) {
        int from = rightFactIdx * groupBitLength + leftFactIdx * getModBitLength();
        int to = rightFactIdx * groupBitLength + (leftFactIdx + 1) * getModBitLength();
        List<FieldElement> subFactors = feZeroSeeds.subList(from, to);
        FieldElement recombined = getFieldElementUtils().recombine(subFactors);
        productShares.add(recombined.negate());
      }
    }
    return productShares;
  }

  List<Pair<FieldElement, FieldElement>> seedsToFieldElements(
      List<Pair<StrictBitVector, StrictBitVector>> seedPairs, BigInteger modulus,
      int modBitLength) {
    // TODO need to check somewhere that the modulus is close enough to 2^modBitLength
    return seedPairs.stream().map(pair -> {
      FieldElement t0 = new FieldElement(new BigInteger(pair.getFirst().toByteArray()).mod(modulus),
          modulus, modBitLength);
      FieldElement t1 = new FieldElement(
          new BigInteger(pair.getSecond().toByteArray()).mod(modulus), modulus, modBitLength);
      return new Pair<>(t0, t1);
    }).collect(Collectors.toList());
  }

  /**
   * Computes product shares. <br>
   * For each right factor r and left factor group of other party (l0, l1, ...), computes secret
   * shares of products r * l0, r*l1, ... .
   * 
   * @param rightFactors this party's factors
   * @return product shares
   */
  public List<FieldElement> multiply(List<FieldElement> rightFactors) {
    List<Pair<StrictBitVector, StrictBitVector>> seedPairs =
        generateSeeds(rightFactors.size(), getModBitLength());

    // convert seeds pairs to field elements so we can compute on them
    List<Pair<FieldElement, FieldElement>> feSeedPairs =
        seedsToFieldElements(seedPairs, getModulus(), getModBitLength());

    // compute q0 - q1 + b for each seed pair
    List<FieldElement> diffs = computeDiffs(feSeedPairs, rightFactors);

    // send diffs over to other party
    sendDiffs(diffs);

    // get zero index seeds
    List<FieldElement> feZeroSeeds =
        feSeedPairs.stream().map(Pair::getFirst).collect(Collectors.toList());

    // compute product shares
    return computeProductShares(feZeroSeeds, rightFactors.size());
  }

}
