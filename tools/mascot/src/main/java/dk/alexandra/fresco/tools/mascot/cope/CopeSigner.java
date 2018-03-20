package dk.alexandra.fresco.tools.mascot.cope;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeftHelper;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Actively-secure implementation of the <i>signer</i> party's side of the Correlated Oblivious
 * Product Evaluation (COPE) protocol.
 *
 * <p>COPE allows two parties, the <i>inputter</i> and the <i>signer</i>, where the inputter holds
 * input values <i>e<sub>1</sub>, ..., e<sub>n</sub></i>, and the signer holds single value <i>s</i>
 * to secret-shared result of <i>s * e<sub>1</sub>, ..., s * e<sub>n</sub></i>. This side of the
 * protocol is to be run by the signer party. For the other side of the protocol, see {@link
 * CopeInputter}.</p>
 */
public class CopeSigner {

  private final List<FieldElementPrg> prgs;
  private final FieldElement macKeyShare;
  private final MultiplyLeftHelper multiplier;
  private final int otherId;
  private final MascotResourcePool resourcePool;
  private final Network network;

  /**
   * Creates new cope signer.
   *
   * <p>This will run the initialization sub-protocol of COPE using an OT protocol to set up the PRG
   * seeds used in the <i>Extend</i> sub-protocol.</p>
   *
   * @param resourcePool The resource pool for the protocol
   * @param network the network to use for communication
   * @param otherId the id of the other party
   * @param macKeyShare this party's share of the mac key
   */
  public CopeSigner(MascotResourcePool resourcePool, Network network, int otherId,
      FieldElement macKeyShare) {
    this.otherId = otherId;
    this.resourcePool = resourcePool;
    this.network = network;
    this.macKeyShare = macKeyShare;
    this.multiplier = new MultiplyLeftHelper(resourcePool, network, otherId);
    this.prgs = new ArrayList<>();
    seedPrgs(multiplier.generateSeeds(macKeyShare, resourcePool.getLambdaSecurityParam()));
  }

  /**
   * Computes shares of product of this party's mac key share and other party's inputs.
   *
   * @param numInputs number of other party's inputs
   * @return shares of product
   */
  public List<FieldElement> extend(int numInputs) {
    // compute chosen masks
    List<FieldElement> chosenMasks = generateMasks(numInputs, resourcePool.getModulus(),
        resourcePool.getModBitLength());
    // use mac share for each input
    List<FieldElement> macKeyShares =
        IntStream.range(0, numInputs).mapToObj(idx -> macKeyShare).collect(Collectors.toList());
    // receive diffs from other party
    List<FieldElement> diffs = resourcePool.getFieldElementSerializer()
        .deserializeList(network.receive(otherId));
    // compute product shares
    return multiplier.computeProductShares(macKeyShares, chosenMasks, diffs);
  }

  private List<FieldElement> generateMasks(int numInputs, BigInteger modulus, int modBitLength) {
    // for each input pair, we use our prgs to get the next set of masks
    List<FieldElement> masks = new ArrayList<>();
    // generate mask for each input
    for (int i = 0; i < numInputs; i++) {
      // generate masks for single input
      List<FieldElement> singleInputMasks = prgs.parallelStream()
          .map(prg -> prg.getNext(modulus))
          .collect(Collectors.toList());
      masks.addAll(singleInputMasks);
    }
    return masks;
  }

  private void seedPrgs(List<StrictBitVector> seeds) {
    for (StrictBitVector seed : seeds) {
      prgs.add(new FieldElementPrgImpl(seed));
    }
  }

}
