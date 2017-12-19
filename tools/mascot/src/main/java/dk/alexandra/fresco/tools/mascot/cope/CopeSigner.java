package dk.alexandra.fresco.tools.mascot.cope;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Actively-secure implementation of the signer party's side of the Correlated Oblivious Product
 * Evaluation (COPE) protocol.<br>
 * COPE allows two parties, the inputter and the signer, where the inputter holds input values e1,
 * ..., en, and the signer holds single value s to secret-shared result of s * e1, ..., s * en. <br>
 * This side of the protocol is to be run by the signer party. For the other side of the protocol,
 * see {@link CopeInputter}.
 * 
 */
public class CopeSigner extends CopeShared {

  private final List<FieldElementPrg> prgs;
  private final FieldElement macKeyShare;
  private final MultiplyLeft multiplier;

  /**
   * Creates new cope signer.
   * 
   */
  public CopeSigner(MascotResourcePool resourcePool, Network network, Integer otherId,
      FieldElement macKeyShare) {
    super(resourcePool, network, otherId);
    this.macKeyShare = macKeyShare;
    this.multiplier = new MultiplyLeft(resourcePool, network, otherId);
    this.prgs = new ArrayList<>();
    seedPrgs(multiplier.generateSeeds(macKeyShare, getLambdaSecurityParam()));
  }

  void seedPrgs(List<StrictBitVector> seeds) {
    for (StrictBitVector seed : seeds) {
      prgs.add(new FieldElementPrgImpl(seed));
    }
  }

  List<FieldElement> generateMasks(int numInputs, BigInteger modulus, int modBitLength) {
    // for each input pair, we use our prgs to get the next set of masks
    List<FieldElement> masks = new ArrayList<>();
    // generate mask for each input
    for (int i = 0; i < numInputs; i++) {
      // generate masks for single input
      List<FieldElement> singleInputMasks =
          prgs.stream().map(prg -> prg.getNext(modulus, modBitLength)).collect(Collectors.toList());
      masks.addAll(singleInputMasks);
    }
    return masks;
  }

  /**
   * Computes shares of product of this party's mac key share and other party's inputs.
   * 
   * @param numInputs number of other party's inputs
   * @return shares of product
   */
  public List<FieldElement> extend(int numInputs) {
    // compute chosen masks
    List<FieldElement> chosenMasks = generateMasks(numInputs, getModulus(), getModBitLength());

    // get diffs from other party
    List<FieldElement> diffs = multiplier.receiveDiffs(numInputs * prgs.size());

    // use mac share for each input
    List<FieldElement> macKeyShares =
        IntStream.range(0, numInputs).mapToObj(idx -> macKeyShare).collect(Collectors.toList());

    // compute product shares
    return multiplier.computeProductShares(macKeyShares, chosenMasks, diffs);
  }

}
