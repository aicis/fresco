package dk.alexandra.fresco.tools.mascot.bit;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.elgen.ElementGeneration;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import dk.alexandra.fresco.tools.mascot.online.OnlinePhase;
import dk.alexandra.fresco.tools.mascot.triple.TripleGeneration;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Test;

public class TestBitConverter extends NetworkedTest {

  private FieldElementPrg getJointPrg(int prgSeedLength) {
    return new FieldElementPrgImpl(new StrictBitVector(prgSeedLength));
  }

  private List<MascotFieldElement> runConvertToBits(MascotTestContext ctx, MascotFieldElement macKeyShare,
      List<MascotFieldElement> randomValues) {
    FieldElementPrg prg = getJointPrg(ctx.getPrgSeedLength());
    ElementGeneration elementGeneration = new ElementGeneration(ctx.getResourcePool(),
        ctx.getNetwork(), macKeyShare, prg);
    OnlinePhase onlinePhase = new OnlinePhase(ctx.getResourcePool(),
        new TripleGeneration(ctx.getResourcePool(), ctx.getNetwork(),
            elementGeneration, prg),
        elementGeneration, macKeyShare);
    BitConverter bitConverter = new BitConverter(ctx.getResourcePool(), onlinePhase, macKeyShare);
    List<AuthenticatedElement> closed = (ctx.getMyId() == 1) ? elementGeneration.input(randomValues)
        : elementGeneration.input(1, randomValues.size());
    List<AuthenticatedElement> bits = bitConverter.convertToBits(closed);
    List<MascotFieldElement> opened = elementGeneration.open(bits);
    elementGeneration.check(bits, opened);
    return opened;
  }

  @Test
  public void testTwoPartiesBatchedConvertToBits() {
    initContexts(2);

    // left party mac key share
    MascotFieldElement macKeyShareOne = new MascotFieldElement(new BigInteger("11231"), getModulus());

    // right party mac key share
    MascotFieldElement macKeyShareTwo = new MascotFieldElement(new BigInteger("7719"), getModulus());

    // party one inputs
    List<MascotFieldElement> randomValues =
        MascotTestUtils.generateSingleRow(new int[]{12, 11, 1, 2}, getModulus());

    // define task each party will run
    Callable<List<MascotFieldElement>> partyOneTask =
        () -> runConvertToBits(contexts.get(1), macKeyShareOne, randomValues);
    Callable<List<MascotFieldElement>> partyTwoTask =
        () -> runConvertToBits(contexts.get(2), macKeyShareTwo, randomValues);

    List<List<MascotFieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
    List<MascotFieldElement> partyOneOutput = results.get(0);
    List<MascotFieldElement> partyTwoOutput = results.get(1);

    // outputs should be same
    CustomAsserts.assertEquals(partyOneOutput, partyTwoOutput);

    // outputs should be bits
    for (MascotFieldElement actualBit : partyOneOutput) {
      CustomAsserts.assertFieldElementIsBit(actualBit);
    }
  }

}
