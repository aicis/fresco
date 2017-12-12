package dk.alexandra.fresco.tools.mascot;

import java.util.List;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.cointossing.CoinTossingMpc;
import dk.alexandra.fresco.tools.mascot.elgen.ElGen;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.triple.TripleGen;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.PaddingPrg;

public class Mascot {

  TripleGen tripleGen;
  ElGen elGen;

  public Mascot(MascotContext ctx) {
    // agree on joint seed
    StrictBitVector jointSeed = new CoinTossingMpc(ctx).generateJointSeed(ctx.getPrgSeedLength());
    FieldElementPrg jointSampler = new PaddingPrg(jointSeed);
    this.elGen = new ElGen(ctx, ctx.getMacKeyShare(), jointSampler);
    this.tripleGen = new TripleGen(ctx, elGen, jointSampler);
  }

  public Mascot(Integer myId, List<Integer> partyIds) {
    this(MascotContext.defaultContext(myId, partyIds));
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
