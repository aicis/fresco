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

  private List<Pair<FieldElement, FieldElement>> seedsToFieldElements(
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
