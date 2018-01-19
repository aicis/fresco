package dk.alexandra.fresco.tools.mascot.online;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
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

  private List<FieldElement> runMultiply(MascotTestContext ctx, FieldElement macKeyShare,
      List<FieldElement> inputs) {
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
    List<FieldElement> opened = elementGeneration.open(products);
    elementGeneration.check(products, opened);
    return opened;
  }

  @Test
  public void testTwoPartiesBatchedMultiply() {
    initContexts(Arrays.asList(1, 2));

    // left party mac key share
    FieldElement macKeyShareOne = new FieldElement(new BigInteger("11231"), modulus);

    // right party mac key share
    FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus);

    // party one inputs
    List<FieldElement> partyOneInputs =
        MascotTestUtils.generateSingleRow(new int[]{12, 11, 1, 2}, modulus);
    // party two inputs
    List<FieldElement> partyTwoInputs =
        MascotTestUtils.generateSingleRow(new int[]{0, 3, 221, 65518}, modulus);

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask =
        () -> runMultiply(contexts.get(1), macKeyShareOne, partyOneInputs);
    Callable<List<FieldElement>> partyTwoTask =
        () -> runMultiply(contexts.get(2), macKeyShareTwo, partyTwoInputs);

    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    List<FieldElement> partyOneOutput = results.get(0);
    List<FieldElement> partyTwoOutput = results.get(1);

    // outputs should be same
    CustomAsserts.assertEquals(partyOneOutput, partyTwoOutput);

    // outputs should be correct products
    List<FieldElement> expected = MascotTestUtils
        .generateSingleRow(new int[]{0, 33, 221, 65517}, modulus);
    CustomAsserts.assertEquals(expected, partyOneOutput);
  }

}
