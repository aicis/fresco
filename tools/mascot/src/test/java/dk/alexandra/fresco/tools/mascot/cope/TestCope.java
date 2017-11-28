package dk.alexandra.fresco.tools.mascot.cope;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.TestRuntime;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestCope {

  private TestRuntime testRuntime;

  @Before
  public void initializeRuntime() {
    testRuntime = new TestRuntime();
  }

  @After
  public void teardownRuntime() {
    testRuntime.shutdown();
    testRuntime = null;
  }

  private List<FieldElement> runSigner(MascotContext ctx, Integer otherId, FieldElement macKeyShare,
      int numExtends) {
    CopeSigner signer = new CopeSigner(ctx, otherId, macKeyShare);
    signer.initialize();
    List<FieldElement> shares = signer.extend(numExtends);
    return shares;
  }

  private List<FieldElement> runInputter(MascotContext ctx, Integer otherId,
      List<FieldElement> inputs) {
    CopeInputter inputter = new CopeInputter(ctx, otherId);
    inputter.initialize();
    List<FieldElement> shares = inputter.extend(inputs);
    return shares;
  }

  @Test
  public void testSingleExtend() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      // left parties input (can be multiple)
      FieldElement macKeyShare = new FieldElement(new BigInteger("11231"), partyOneCtx.getModulus(),
          partyOneCtx.getkBitLength());

      // single right party input element
      FieldElement input =
          new FieldElement(7, partyOneCtx.getModulus(), partyTwoCtx.getkBitLength());
      List<FieldElement> inputs = Arrays.asList(input);

      // define task each party will run
      Callable<List<FieldElement>> partyOneTask = () -> runSigner(partyOneCtx, 2, macKeyShare, 1);
      Callable<List<FieldElement>> partyTwoTask = () -> runInputter(partyTwoCtx, 1, inputs);

      // run tasks and get ordered list of results
      List<List<FieldElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // get per party results
      List<FieldElement> leftResults = results.get(0);
      List<FieldElement> rightResults = results.get(1);

      FieldElement expected = macKeyShare.multiply(input);
      FieldElement actual = leftResults.get(0)
          .add(rightResults.get(0));
      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testBatchedExtend() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      BigInteger modulus = partyOneCtx.getModulus();
      int modBitLength = partyOneCtx.getkBitLength();

      // left parties input (can be multiple)
      FieldElement macKeyShare = new FieldElement(new BigInteger("11231"), partyOneCtx.getModulus(),
          partyOneCtx.getkBitLength());

      // multiple input elements
      int[] inputArr = {7, 444, 112, 11};
      List<FieldElement> inputs =
          MascotTestUtils.generateSingleRow(inputArr, modulus, modBitLength);

      // define task each party will run
      Callable<List<FieldElement>> partyOneTask =
          () -> runSigner(partyOneCtx, 2, macKeyShare, inputs.size());
      Callable<List<FieldElement>> partyTwoTask = () -> runInputter(partyTwoCtx, 1, inputs);

      // run tasks and get ordered list of results
      List<List<FieldElement>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // get per party results
      List<FieldElement> leftResults = results.get(0);
      List<FieldElement> rightResults = results.get(1);

      int[] expectedArr = {7 * 11231 % modulus.intValue(), 444 * 11231 % modulus.intValue(),
          112 * 11231 % modulus.intValue(), 11 * 11231 % modulus.intValue()};
      List<FieldElement> expected =
          MascotTestUtils.generateSingleRow(expectedArr, modulus, modBitLength);

      List<FieldElement> actual = IntStream.range(0, expected.size())
          .mapToObj(idx -> leftResults.get(idx)
              .add(rightResults.get(idx)))
          .collect(Collectors.toList());

      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

  @Test
  public void testUnequalSecParamAndBitLength() {
    // TODO
  }

}
