package dk.alexandra.fresco.tools.mascot;

import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.cointossing.CoinTossingMpc;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.triple.TripleGeneration;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.PaddingPrg;

public class Mascot {

  TripleGeneration tripleGeneration;
  ElementGeneration elementGeneration;

  public Mascot(MascotResourcePool resourcePool, Network network, FieldElement macKeyShare) {
    // agree on joint seed
    StrictBitVector jointSeed = new CoinTossingMpc(resourcePool, network).generateJointSeed(resourcePool.getPrgSeedLength());
    FieldElementPrg jointSampler = new PaddingPrg(jointSeed);
    this.elementGeneration = new ElementGeneration(resourcePool, network, macKeyShare, jointSampler);
    this.tripleGeneration = new TripleGeneration(resourcePool, network, elementGeneration, jointSampler);
    // triple generation will also initialize element generation
    this.tripleGeneration.initialize();
  }

  public List<MultTriple> getTriples(int numTriples) {
    return tripleGeneration.triple(numTriples);
  }

  public List<AuthenticatedElement> getElements(List<FieldElement> rawElements) {
    return elementGeneration.input(rawElements);
  }

  public List<AuthenticatedElement> getElements(Integer inputterId, int numElements) {
    return elementGeneration.input(inputterId, numElements);
  }

}
