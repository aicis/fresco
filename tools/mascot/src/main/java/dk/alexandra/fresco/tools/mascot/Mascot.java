package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.arithm.ArithmeticCollectionUtils;
import dk.alexandra.fresco.tools.mascot.cointossing.CoinTossingMpc;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.triple.TripleGeneration;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the main MASCOT protocol (https://eprint.iacr.org/2016/505.pdf) which can be
 * used for the SPDZ pre-processing phase. <br>
 * Supports generation of multiplication triples, and random authenticated elements.
 */
public class Mascot extends BaseProtocol {

  private final TripleGeneration tripleGeneration;
  private final ElementGeneration elementGeneration;

  /**
   * Creates new {@link Mascot}.
   */
  public Mascot(MascotResourcePool resourcePool, Network network, FieldElement macKeyShare) {
    super(resourcePool, network);
    // agree on joint seed
    StrictBitVector jointSeed = new CoinTossingMpc(resourcePool, network)
        .generateJointSeed(resourcePool.getPrgSeedLength());
    FieldElementPrg jointSampler = new FieldElementPrgImpl(jointSeed);
    this.elementGeneration =
        new ElementGeneration(resourcePool, network, macKeyShare, jointSampler);
    this.tripleGeneration =
        new TripleGeneration(resourcePool, network, elementGeneration, jointSampler);
  }

  /**
   * Generates a batch of multiplication triples.
   * 
   * @param numTriples number of triples in batch
   * @return multiplication triples
   */
  public List<MultTriple> getTriples(int numTriples) {
    return tripleGeneration.triple(numTriples);
  }

  /**
   * Runs the input functionality on a batch of field elements. <br>
   * Allows a party to turn unauthenticated, private field elements into a secret-shared
   * authenticated elements. <br>
   * The party holding the input elements should call this method.
   * 
   * @param rawElements field elements to input
   * @return this party's authenticated shares of the inputs
   */
  public List<AuthenticatedElement> input(List<FieldElement> rawElements) {
    return elementGeneration.input(rawElements);
  }

  /**
   * Same as {@link #input(List)} but to be called by non-input parties.
   * 
   * @param inputterId the id of the inputter
   * @param numElements number of input elements
   * @return this party's authenticated shares of the inputs
   */
  public List<AuthenticatedElement> input(Integer inputterId, int numElements) {
    return elementGeneration.input(inputterId, numElements);
  }

  /**
   * Creates random authenticated elements.
   * 
   * @param numElements number of elements to create
   * @return random authenticated elements
   */
  public List<AuthenticatedElement> getRandomElements(int numElements) {
    List<List<AuthenticatedElement>> perPartyElements = new ArrayList<>(getPartyIds().size());
    for (Integer partyId : getPartyIds()) {
      if (partyId.equals(getMyId())) {
        List<FieldElement> randomElements =
            getLocalSampler().getNext(getModulus(), getModBitLength(), numElements);
        perPartyElements.add(elementGeneration.input(randomElements));
      } else {
        perPartyElements.add(elementGeneration.input(partyId, numElements));
      }
    }
    return new ArithmeticCollectionUtils<AuthenticatedElement>().pairwiseSum(perPartyElements);
  }

}