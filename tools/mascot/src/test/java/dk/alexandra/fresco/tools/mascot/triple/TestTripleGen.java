package dk.alexandra.fresco.tools.mascot.triple;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestTripleGen extends NetworkedTest {

  private List<List<FieldElement>> runSinglePartyMult(MascotContext ctx, FieldElement macKeyShare,
      List<List<FieldElement>> leftFactorGroups, List<FieldElement> rightFactors) throws Exception {
    int numLeftFactors = leftFactorGroups.get(0)
        .size();
    TripleGen tripleGen = new TripleGen(ctx, macKeyShare, numLeftFactors);
    tripleGen.initialize();
    List<List<FieldElement>> productGroups = tripleGen.multiply(leftFactorGroups, rightFactors);
    return productGroups;
  }

  @Test
  public void testTwoPartiesSingleMult() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

      // left party mac key share
      FieldElement macKeyShareOne =
          new FieldElement(new BigInteger("11231"), modulus, modBitLength);

      // right party mac key share
      FieldElement macKeyShareTwo = new FieldElement(new BigInteger("7719"), modulus, modBitLength);

      // party one input
      int[][] leftArrOne = {{12}};
      List<List<FieldElement>> leftFactorsOne =
          MascotTestUtils.generateLeftInput(leftArrOne, modulus, modBitLength);
      int[] rightArrOne = {11};
      List<FieldElement> rightFactorsOne =
          MascotTestUtils.generateSingleRow(rightArrOne, modulus, modBitLength);

      // party two input
      int[][] leftArrTwo = {{123}};
      List<List<FieldElement>> leftFactorsTwo =
          MascotTestUtils.generateLeftInput(leftArrTwo, modulus, modBitLength);
      int[] rightArrTwo = {2222};
      List<FieldElement> rightFactorsTwo =
          MascotTestUtils.generateSingleRow(rightArrTwo, modulus, modBitLength);

      // define task each party will run
      Callable<List<List<FieldElement>>> partyOneTask =
          () -> runSinglePartyMult(partyOneCtx, macKeyShareOne, leftFactorsOne, rightFactorsOne);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runSinglePartyMult(partyTwoCtx, macKeyShareTwo, leftFactorsTwo, rightFactorsTwo);

      // wow much list amaze
      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
      System.out.println(results);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

}
