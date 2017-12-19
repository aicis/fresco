package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.arithm.CollectionUtils;
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

public class Mascot extends BaseProtocol {

  TripleGeneration tripleGeneration;
  ElementGeneration elementGeneration;

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

  public List<MultTriple> getTriples(int numTriples) {
    return tripleGeneration.triple(numTriples);
  }

  public List<AuthenticatedElement> input(List<FieldElement> rawElements) {
    return elementGeneration.input(rawElements);
  }

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
    return CollectionUtils.pairwiseSum(perPartyElements);
  }

}
