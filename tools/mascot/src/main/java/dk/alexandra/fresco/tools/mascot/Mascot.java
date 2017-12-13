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

  TripleGeneration tripleGen;
  ElementGeneration elGen;

  public Mascot(MascotResourcePool resourcePool, Network network, FieldElement macKeyShare) {
    // agree on joint seed
    StrictBitVector jointSeed = new CoinTossingMpc(resourcePool, network).generateJointSeed(resourcePool.getPrgSeedLength());
    FieldElementPrg jointSampler = new PaddingPrg(jointSeed);
    this.elGen = new ElementGeneration(resourcePool, network, macKeyShare, jointSampler);
    this.tripleGen = new TripleGeneration(resourcePool, network, elGen, jointSampler);
  }

  public void initialize() {
    // triple-gen will also initialize el-gen
    this.tripleGen.initialize();
  }

  public List<MultTriple> getTriples(int numTriples) {
    return tripleGen.triple(numTriples);
  }

  public List<AuthenticatedElement> getElements(List<FieldElement> rawElements) {
    return elGen.input(rawElements);
  }

  public List<AuthenticatedElement> getElements(Integer inputterId, int numElements) {
    return elGen.input(inputterId, numElements);
  }

}
