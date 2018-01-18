package dk.alexandra.fresco.tools.mascot.cope;

import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.MascotTestContext;
import dk.alexandra.fresco.tools.mascot.MascotTestUtils;
import dk.alexandra.fresco.tools.mascot.NetworkedTest;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class TestCope extends NetworkedTest {

  private List<FieldElement> runSigner(MascotTestContext ctx, Integer otherId,
      FieldElement macKeyShare, int numExtends) {
    CopeSigner signer =
        new CopeSigner(ctx.getResourcePool(), ctx.getNetwork(), otherId, macKeyShare);
    List<FieldElement> shares = signer.extend(numExtends);
    return shares;
  }

  private List<FieldElement> runInputter(MascotTestContext ctx, Integer otherId,
      List<FieldElement> inputs) {
    CopeInputter inputter = new CopeInputter(ctx.getResourcePool(), ctx.getNetwork(), otherId);
    List<FieldElement> shares = inputter.extend(inputs);
    return shares;
  }

  @Test
  public void testSingleExtend() {
    initContexts(Arrays.asList(1, 2));

    // left parties input (can be multiple)
    FieldElement macKeyShare = new FieldElement(new BigInteger("11231"), modulus);

    // single right party input element
    FieldElement input = new FieldElement(7, modulus);
    List<FieldElement> inputs = Collections.singletonList(input);

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask = () -> runSigner(contexts.get(1), 2, macKeyShare, 1);
    Callable<List<FieldElement>> partyTwoTask = () -> runInputter(contexts.get(2), 1, inputs);

    // run tasks and get ordered list of results
    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // get per party results
    List<FieldElement> leftResults = results.get(0);
    List<FieldElement> rightResults = results.get(1);

    FieldElement expected = macKeyShare.multiply(input);
    FieldElement actual = leftResults.get(0).add(rightResults.get(0));
    CustomAsserts.assertEquals(expected, actual);
  }

  void testBatchedExtend(int lambdaSecurityParam) {
    this.lambdaSecurityParam = lambdaSecurityParam;
    initContexts(Arrays.asList(1, 2));

    // left parties input (can be multiple)
    FieldElement macKeyShare = new FieldElement(new BigInteger("11231"), modulus);

    // multiple input elements
    int[] inputArr = {7, 444, 112, 11};
    List<FieldElement> inputs = MascotTestUtils.generateSingleRow(inputArr, modulus);

    // define task each party will run
    Callable<List<FieldElement>> partyOneTask =
        () -> runSigner(contexts.get(1), 2, macKeyShare, inputs.size());
    Callable<List<FieldElement>> partyTwoTask = () -> runInputter(contexts.get(2), 1, inputs);

    // run tasks and get ordered list of results
    List<List<FieldElement>> results =
        testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

    // get per party results
    List<FieldElement> leftResults = results.get(0);
    List<FieldElement> rightResults = results.get(1);

    int[] expectedArr = {7 * 11231 % modulus.intValue(), 444 * 11231 % modulus.intValue(),
        112 * 11231 % modulus.intValue(), 11 * 11231 % modulus.intValue()};
    List<FieldElement> expected =
        MascotTestUtils.generateSingleRow(expectedArr, modulus);

    List<FieldElement> actual = IntStream.range(0, expected.size())
        .mapToObj(idx -> leftResults.get(idx).add(rightResults.get(idx)))
        .collect(Collectors.toList());

    CustomAsserts.assertEquals(expected, actual);
  }

  @Test
  public void testEqualSecParamAndBitLength() {
    testBatchedExtend(this.lambdaSecurityParam);
  }

  @Test
  public void testUnequalSecParamAndBitLength() {
    testBatchedExtend(24);
  }

}
