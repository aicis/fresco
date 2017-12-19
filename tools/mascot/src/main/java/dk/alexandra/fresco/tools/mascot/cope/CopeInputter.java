package dk.alexandra.fresco.tools.mascot.cope;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Actively-secure implementation of the inputter party's side of the Correlated Oblivious Product
 * Evaluation (COPE) protocol.<br>
 * COPE allows two parties, the inputter and the signer, where the inputter holds input values e1,
 * ..., en, and the signer holds single value s to secret-shared result of s * e1, ..., s * en. <br>
 * This side of the protocol is to be run by the inputter party. For the other side of the protocol,
 * see {@link CopeSigner}.
 * 
 */
public class CopeInputter extends CopeShared {

  private final List<FieldElementPrg> leftPrgs;
  private final List<FieldElementPrg> rightPrgs;
  private final MultiplyRight multiplier;

  /**
   * Creates new {@link CopeInputter}.
   */
  public CopeInputter(MascotResourcePool resourcePool, Network network, Integer otherId) {
    super(resourcePool, network, otherId);
    this.leftPrgs = new ArrayList<>();
    this.rightPrgs = new ArrayList<>();
    this.multiplier = new MultiplyRight(resourcePool, network, otherId);
    seedPrgs(multiplier.generateSeeds(1, getLambdaSecurityParam()));
  }

  void seedPrgs(List<Pair<StrictBitVector, StrictBitVector>> seeds) {
    for (Pair<StrictBitVector, StrictBitVector> seedPair : seeds) {
      this.leftPrgs.add(new FieldElementPrgImpl(seedPair.getFirst()));
      this.rightPrgs.add(new FieldElementPrgImpl(seedPair.getSecond()));
    }
  }

  List<Pair<FieldElement, FieldElement>> generateMaskPairs(BigInteger modulus, int modBitLength) {
    Stream<Pair<FieldElement, FieldElement>> maskStream =
        IntStream.range(0, leftPrgs.size()).mapToObj(idx -> {
          FieldElement t0 = this.leftPrgs.get(idx).getNext(modulus, modBitLength);
          FieldElement t1 = this.rightPrgs.get(idx).getNext(modulus, modBitLength);
          return new Pair<>(t0, t1);
        });
    return maskStream.collect(Collectors.toList());
  }

  List<Pair<FieldElement, FieldElement>> generateMaskPairs(int numInputs) {
    // for each input pair, we use our prf to get the next set of masks
    List<Pair<FieldElement, FieldElement>> maskPairs = new ArrayList<>();
    for (int i = 0; i < numInputs; i++) {
      // generate masks for single input
      maskPairs.addAll(generateMaskPairs(getModulus(), getModBitLength()));
    }
    return maskPairs;
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

    // compute q0 - q1 + x for each input x for each mask pair
    List<FieldElement> diffs = multiplier.computeDiffs(maskPairs, inputElements);

    // send diffs
    multiplier.sendDiffs(diffs);

    // get zero index masks
    List<FieldElement> feZeroSeeds =
        maskPairs.stream().map(feSeedPair -> feSeedPair.getFirst()).collect(Collectors.toList());

    // compute product shares
    List<FieldElement> productShares =
        multiplier.computeProductShares(feZeroSeeds, inputElements.size());

    return productShares;
  }
}
