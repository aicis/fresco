package dk.alexandra.fresco.tools.mascot.cope;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRightHelper;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Actively-secure implementation of the inputter party's side of the Correlated Oblivious Product
 * Evaluation (COPE) protocol.
 *
 * <p>COPE allows two parties, the <i>inputter</i> and the <i>signer</i>, where the inputter holds
 * input values <i>e<sub>1</sub>, ..., e<sub>n</sub></i>, and the signer holds single value <i>s</i>
 * to secret-shared result of <i>s * e<sub>1</sub>, ..., s * e<sub>n</sub></i>. This side of the
 * protocol is to be run by the inputter party. For the other side of the protocol, see {@link
 * CopeSigner}.</p>
 */
public class CopeInputter {

  private final List<FieldElementPrg> leftPrgs;
  private final List<FieldElementPrg> rightPrgs;
  private final MultiplyRightHelper helper;
  private final int otherId;
  private final MascotResourcePool resourcePool;
  private final Network network;

  /**
   * Creates a new {@link CopeInputter} and initializes the COPE protocol.
   *
   * <p>This will run the initialization sub-protocol of COPE using an OT protocol to set up the PRG
   * seeds used in the <i>Extend</i> sub-protocol.</p>
   */
  public CopeInputter(MascotResourcePool resourcePool, Network network, int otherId) {
    this.otherId = otherId;
    this.resourcePool = resourcePool;
    this.network = network;
    this.leftPrgs = new ArrayList<>();
    this.rightPrgs = new ArrayList<>();
    this.helper = new MultiplyRightHelper(resourcePool, network, otherId);
    seedPrgs(helper.generateSeeds(1, resourcePool.getLambdaSecurityParam()));
  }

  /**
   * Computes shares of products of this party's input elements and other party's mac key share.
   *
   * @param inputElements input field elements
   * @return shares of products of mac key share and input elements
   */
  public List<FieldElement> extend(List<FieldElement> inputElements) {
    // use seeds to generate mask pairs
    List<Pair<FieldElement, FieldElement>> maskPairs = generateMaskPairs(inputElements.size());
    // compute t0 - t1 + x for each input x for each mask pair
    List<FieldElement> diffs = helper.computeDiffs(maskPairs, inputElements);
    // send diffs
    network.send(otherId, resourcePool.getFieldElementSerializer().serialize(diffs));
    // get zero index masks
    List<FieldElement> feZeroSeeds =
        maskPairs.stream().map(Pair::getFirst).collect(Collectors.toList());
    // compute product shares
    return helper.computeProductShares(feZeroSeeds, inputElements.size());
  }

  private List<Pair<FieldElement, FieldElement>> generateMaskPairs(int numInputs) {
    // for each input pair, we use our prf to get the next set of masks
    List<Pair<FieldElement, FieldElement>> maskPairs = new ArrayList<>();
    for (int i = 0; i < numInputs; i++) {
      // generate masks for single input
      maskPairs.addAll(generateMaskPairs(resourcePool.getModulus()));
    }
    return maskPairs;
  }

  private List<Pair<FieldElement, FieldElement>> generateMaskPairs(BigInteger modulus) {
    Stream<Pair<FieldElement, FieldElement>> maskStream =
        IntStream.range(0, leftPrgs.size()).mapToObj(idx -> {
          FieldElement t0 = this.leftPrgs.get(idx).getNext(modulus);
          FieldElement t1 = this.rightPrgs.get(idx).getNext(modulus);
          return new Pair<>(t0, t1);
        });
    return maskStream.collect(Collectors.toList());
  }

  private void seedPrgs(List<Pair<StrictBitVector, StrictBitVector>> seeds) {
    for (Pair<StrictBitVector, StrictBitVector> seedPair : seeds) {
      this.leftPrgs.add(new FieldElementPrgImpl(seedPair.getFirst()));
      this.rightPrgs.add(new FieldElementPrgImpl(seedPair.getSecond()));
    }
  }

}
