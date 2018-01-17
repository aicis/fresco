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
 * Right hand side of the actively-secure two-party protocol for computing a secret sharing of a the
 * scalar product <i><b>c</b>= <b>a</b>b</i> of a vector <i><b>a</b></i> held by the <i>left</i>
 * party and a factor <i>b</i> held by the <i>right</i> party.
 *
 * <p>
 * This protocol corresponds to steps 1 and 2 of the <i>Multiply</i> sub-protocol of the
 * <i>&Pi;<sub>Triple</sub></i> protocol of the MASCOT paper. This implementation will batch runs of
 * a number these protocols. I.e., it will compute the secret sharing of a number of scalar product
 * in one batch.
 * </p>
 * <p>
 * This class implements the functionality of the right party. For the other side, see
 * {@link MultiplyLeft}. The resulting scalar product is secret-shared among the two parties.
 * </p>
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
   * Runs a batch of the multiply protocol with a given set of right hand factors.
   *
   * <p>
   * For each right factor <i>b</i> and left vector of the other party <i><b>a</b> = (a<sub>0</sub>,
   * a<sub>1</sub>, ...)</i>, the protocol computes secret shares of scalar product
   * <i><b>a</b>b = (a<sub>0</sub>b, a<sub>1</sub>b, ... </i>).
   * </p>
   *
   * @param rightFactors this party's factors <i>b<sub>0</sub>, b<sub>1</sub> ...</i>
   * @return shares of the scalar products <i><b>a</b><sub>0</sub>b<sub>0</sub>,
   * <b>a</b><sub>1</sub>b<sub>1</sub> ... </i>
   */
  public List<FieldElement> multiply(List<FieldElement> rightFactors) {
    List<Pair<StrictBitVector, StrictBitVector>> seedPairs =
        generateSeeds(rightFactors.size(), getModBitLength());
    // convert seeds pairs to field elements pairs (q_0, q_1) so we can compute on them
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

  /**
   * Generate random seed pairs using OT.
   *
   * <p>
   * The seed pairs are correlated with the multiplication factors of the other party. If the other
   * party's factors (represented as a bit vector) is 010, this party will receive seed pairs
   * <i>(a<sub>0</sub>, a<sub>1</sub>), (b<sub>0</sub>, b<sub>1</sub>), (c<sub>0</sub>,
   * c<sub>1</sub>)</i> whereas the other party will receive seeds <i>a<sub>0</sub>, b<sub>1</sub>,
   * c<sub>0</sub></i>. The parties can use the resulting seeds to compute the shares of the product
   * of their factors.
   * </p>
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



  /**
   * Computes "masked" share of each bit of each of this party's factors.
   *
   * <p>
   * For each seed pair <i>(q<sub>0</sub>, q<sub>1</sub>)<sub>n</sub></i> compute <i>q<sub>0</sub> -
   * q<sub>1</sub> + b<sub>n</sub></i> where <i>b<sub>n</sub></i> is the <i>n</i>-th factor of this
   * party's factor.
   * </p>
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

  FieldElement computeDiff(Pair<FieldElement, FieldElement> feSeedPair, FieldElement factor) {
    FieldElement left = feSeedPair.getFirst();
    FieldElement right = feSeedPair.getSecond();
    FieldElement diff = left.subtract(right).add(factor);
    return diff;
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
    return seedPairs.stream().map(pair -> {
      FieldElement t0 = fromBits(pair.getFirst(), modulus, modBitLength);
      FieldElement t1 = fromBits(pair.getSecond(), modulus, modBitLength);
      return new Pair<>(t0, t1);
    }).collect(Collectors.toList());
  }

  private FieldElement fromBits(StrictBitVector vector, BigInteger modulus, int modBitLength) {
    // TODO need to check somewhere that the modulus is close enough to 2^modBitLength
    return new FieldElement(new BigInteger(vector.toByteArray()).mod(modulus), modulus,
        modBitLength);
  }

}
