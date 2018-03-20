package dk.alexandra.fresco.tools.mascot.triple;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeftHelper;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Left hand side of a two-party protocol for computing a secret sharing of a the entry wise product
 * of vectors <i><b>a</b></i> held by the <i>left</i> party and <i><b>b</b></i> held by the
 * <i>right</i> party.
 *
 * <p> This protocol is a generalization of step 2 of the <i>Multiply</i> sub-protocol in the
 * <i>&Pi;<sub>Triple</sub></i> protocol of the MASCOT paper. While step 2 of <i>Multiply</i>
 * computes a secret sharing of a <i>scalar product</i>, this implementation computes the entry wise
 * product. To compute a scalar product using this implementation (as done in the MASCOT paper) we
 * can let all entries of <i><b>b</b></i> be equal. This also allows us to compute multiple scalar
 * products in a single batch letting <i><b>b</b></i> be the concatenation of vectors with equal
 * entries. </p> <p> <b>Note:</b> this class is to be used as a sub-protocol in {@link
 * dk.alexandra.fresco.tools.mascot.triple.TripleGeneration} and may not be secure if used outside
 * of the intended context. </p> <p> This class implements the functionality of the left party. For
 * the other side, see {@link MultiplyRight}. The resulting entry wise product is secret-shared
 * among the two parties. </p>
 */
class MultiplyLeft {

  private final MultiplyLeftHelper multiplyLeftHelper;
  private final int otherId;
  private final MascotResourcePool resourcePool;
  private final Network network;

  /**
   * Constructs one side of the two-party multiplication protocol.
   *
   * @param resourcePool the resource pool
   * @param network the network
   * @param otherId the other party's
   */
  MultiplyLeft(MascotResourcePool resourcePool, Network network, int otherId) {
    this.otherId = otherId;
    this.resourcePool = resourcePool;
    this.network = network;
    multiplyLeftHelper = new MultiplyLeftHelper(resourcePool, network, otherId);
  }

  /**
   * Runs a batch of the entry wise product protocol with a given of left hand vector.
   *
   * <p> For right vector <i><b>b</b>= b<sub>0</sub>, b<sub>1</sub>, ...)</i> and left vector
   * <i><b>a</b> = (a<sub>0</sub>, a<sub>1</sub>, ...)</i>, the protocol computes secret shares of
   * entry wise product <i>(a<sub>0</sub>b<sub>0</sub>, a<sub>1</sub>b<sub>1</sub>, ... </i>). </p>
   *
   * @param leftFactors this party's vector <i>a<sub>0</sub>, a<sub>1</sub> ...</i>
   * @return shares of the products <i>a<sub>0</sub>b<sub>0</sub>, a<sub>1</sub>b<sub>1</sub>
   * ...</i>
   */
  public List<FieldElement> multiply(List<FieldElement> leftFactors) {
    List<StrictBitVector> seeds = multiplyLeftHelper.generateSeeds(leftFactors,
        resourcePool.getModBitLength());
    List<FieldElement> feSeeds = seedsToFieldElements(seeds, resourcePool.getModulus());
    // receive diffs from other party
    List<FieldElement> diffs =
        resourcePool.getFieldElementSerializer()
            .deserializeList(network.receive(otherId));
    return multiplyLeftHelper.computeProductShares(leftFactors, feSeeds, diffs);
  }

  /**
   * Converts each seed to field element.
   *
   * @param seeds the seeds represented as bit vectors
   * @param modulus the modulus we are working in
   * @return seeds converted to field elements
   */
  private List<FieldElement> seedsToFieldElements(List<StrictBitVector> seeds, BigInteger modulus) {
    return seeds.parallelStream().map(seed -> fromBits(seed, modulus)).collect(Collectors.toList());
  }

  private FieldElement fromBits(StrictBitVector vector, BigInteger modulus) {
    // safe since the modulus is guaranteed to be close enough to 2^modBitLength
    return new FieldElement(new BigInteger(vector.toByteArray()).mod(modulus), modulus);
  }

}
