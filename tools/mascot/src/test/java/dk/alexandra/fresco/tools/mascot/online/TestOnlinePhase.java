package dk.alexandra.fresco.tools.mascot.online;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import dk.alexandra.fresco.tools.mascot.triple.TripleGeneration;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Test;

public class TestOnlinePhase extends NetworkedTest {

  private FieldElementPrg getJointPrg(int prgSeedLength) {
    return new FieldElementPrgImpl(new StrictBitVector(prgSeedLength));
  }

  private List<MascotFieldElement> runMultiply(MascotTestContext ctx, MascotFieldElement macKeyShare,
      List<MascotFieldElement> inputs) {
    FieldElementPrg prg = getJointPrg(ctx.getPrgSeedLength());
    ElementGeneration elementGeneration = new ElementGeneration(ctx.getResourcePool(),
        ctx.getNetwork(), macKeyShare, prg);
    TripleGeneration tripleGeneration = new TripleGeneration(ctx.getResourcePool(),
        ctx.getNetwork(),
        elementGeneration, prg);
    OnlinePhase onlinePhase = new OnlinePhase(ctx.getResourcePool(), tripleGeneration,
        elementGeneration, macKeyShare);
    List<AuthenticatedElement> partyOneInputs;
    List<AuthenticatedElement> partyTwoInputs;
    if (ctx.getMyId() == 1) {
      partyOneInputs = elementGeneration.input(inputs);
      partyTwoInputs = elementGeneration.input(2, inputs.size());
    } else {
      partyOneInputs = elementGeneration.input(1, inputs.size());
      partyTwoInputs = elementGeneration.input(inputs);
    }
    List<AuthenticatedElement> products = onlinePhase.multiply(partyOneInputs, partyTwoInputs);
    List<MascotFieldElement> opened = elementGeneration.open(products);
    elementGeneration.check(products, opened);
    return opened;
  }

  @Test
  public void testTwoPartiesBatchedMultiply() {
    initContexts(2);

    // left party mac key share
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());

    // right party mac key share
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    // party one inputs
    List<MascotFieldElement> partyOneInputs =
        MascotTestUtils.generateSingleRow(new int[]{12, 11, 1, 2}, getModulus());
    // party two inputs
    List<MascotFieldElement> partyTwoInputs =
        MascotTestUtils.generateSingleRow(new int[]{0, 3, 221, 65518}, getModulus());

    // define task each party will run
    Callable<List<MascotFieldElement>> partyOneTask =
        () -> runMultiply(contexts.get(1), macKeyShareOne, partyOneInputs);
    Callable<List<MascotFieldElement>> partyTwoTask =
        () -> runMultiply(contexts.get(2), macKeyShareTwo, partyTwoInputs);

    List<List<MascotFieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    List<MascotFieldElement> partyOneOutput = results.get(0);
    List<MascotFieldElement> partyTwoOutput = results.get(1);

    // outputs should be same
    CustomAsserts.assertEquals(partyOneOutput, partyTwoOutput);

    // outputs should be correct products
    List<MascotFieldElement> expected = MascotTestUtils
        .generateSingleRow(new int[]{0, 33, 221, 65517}, getModulus());
    CustomAsserts.assertEquals(expected, partyOneOutput);
  }

}
