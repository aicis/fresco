package dk.alexandra.fresco.tools.mascot.mult;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actively-secure two-party protocol for computing the product of two secret inputs. <br> One input
 * is held by the left party, the other by the right party. The protocol is asymmetric in the sense
 * that the left party performs a different computation from the right party. This class implements
 * the functionality of the left party. For the other side, see {@link MultiplyRight}. The resulting
 * product is secret-shared among the two parties.
 */
public class MultiplyLeft extends TwoPartyProtocol {

  private final MultiplyLeftHelper multiplyLeftHelper;

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
    super(resourcePool, network, otherId);
    multiplyLeftHelper = new MultiplyLeftHelper(resourcePool, network, otherId, numLeftFactors);
  }

  public MultiplyLeft(MascotResourcePool resourcePool, Network network, Integer otherId) {
    this(resourcePool, network, otherId, 1);
  }

  /**
   * Converts each seed to field element using the PRG.
   *
   * @param seeds the seeds represented as bit vectors
   * @param modulus the modulus we are working in
   * @param modBitLength the bit length of the modulus
   * @return seeds converted to field elements via PRG
   */
  private List<FieldElement> seedsToFieldElements(List<StrictBitVector> seeds, BigInteger modulus,
      int modBitLength) {
    // TODO need to check somewhere that the modulus is close enough to 2^modBitLength
    return seeds.stream()
        .map(seed -> new FieldElement(new BigInteger(seed.toByteArray()).mod(modulus), modulus,
            modBitLength)).collect(Collectors.toList());
  }

  /**
   * Computes shares of product of left factors and right factor held by other party. <br> If this
   * party holds l0, l1, l2 and other party holds r0, this will compute additive shares of l0 * r0,
   * l1 * r0, l2 * r0.
   *
   * @param leftFactors this party's factors
   * @return shares of products
   */
  public List<FieldElement> multiply(List<FieldElement> leftFactors) {
    List<StrictBitVector> seeds = multiplyLeftHelper.generateSeeds(leftFactors, getModBitLength());
    List<FieldElement> feSeeds = seedsToFieldElements(seeds, getModulus(), getModBitLength());
    // receive diffs from other party
    List<FieldElement> diffs = getFieldElementSerializer()
        .deserializeList(getNetwork().receive(getOtherId()));
    return multiplyLeftHelper.computeProductShares(leftFactors, feSeeds, diffs);
  }

}
