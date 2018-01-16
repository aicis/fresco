package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actively-secure two-party protocol for computing the product of two secret inputs.
 *
 * <p>One input is held by the left party, the other by the right party. The protocol is asymmetric
 * in the sense that the left party performs a different computation from the right party. This
 * class implements the functionality of the right party. For the other side, see {@link
 * MultiplyLeft}. The resulting product is secret-shared among the two parties.</p>
 */
public class MultiplyRight extends TwoPartyProtocol {

  private final MultiplyRightHelper multiplyRightHelper;

  public MultiplyRight(MascotResourcePool resourcePool, Network network, Integer otherId,
      int numLeftFactors) {
    super(resourcePool, network, otherId);
    multiplyRightHelper = new MultiplyRightHelper(resourcePool, network, otherId, numLeftFactors);
  }

  public MultiplyRight(MascotResourcePool resourcePool, Network network, Integer otherId) {
    this(resourcePool, network, otherId, 1);
  }

  private List<Pair<FieldElement, FieldElement>> seedsToFieldElements(
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
   * Computes product shares. <br> For each right factor r and left factor group of other party (l0,
   * l1, ...), computes secret shares of products r * l0, r*l1, ... .
   *
   * @param rightFactors this party's factors
   * @return product shares
   */
  public List<FieldElement> multiply(List<FieldElement> rightFactors) {
    List<Pair<StrictBitVector, StrictBitVector>> seedPairs =
        multiplyRightHelper.generateSeeds(rightFactors.size(), getModBitLength());

    // convert seeds pairs to field elements so we can compute on them
    List<Pair<FieldElement, FieldElement>> feSeedPairs =
        seedsToFieldElements(seedPairs, getModulus(), getModBitLength());

    // compute q0 - q1 + b for each seed pair
    List<FieldElement> diffs = multiplyRightHelper.computeDiffs(feSeedPairs, rightFactors);

    // send diffs over to other party
    getNetwork().send(getOtherId(), getFieldElementSerializer().serialize(diffs));

    // get zero index seeds
    List<FieldElement> feZeroSeeds =
        feSeedPairs.stream().map(Pair::getFirst).collect(Collectors.toList());

    // compute product shares
    return multiplyRightHelper.computeProductShares(feZeroSeeds, rightFactors.size());
  }

}
