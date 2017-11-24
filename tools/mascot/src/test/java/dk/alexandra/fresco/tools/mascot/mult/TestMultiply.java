package dk.alexandra.fresco.tools.mascot.mult;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.TestRuntime;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestMultiply {

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

  private List<List<FieldElement>> runLeftMult(MascotContext ctx, Integer otherId,
      List<List<FieldElement>> inputs) {
    MultiplyLeft mult = new MultiplyLeft(ctx, otherId, inputs.size());
    return mult.multiply(inputs);
  }

  private List<List<FieldElement>> runRightMult(MascotContext ctx, Integer otherId, List<FieldElement> inputs,
      int numLeftFactors) {
    MultiplyRight mult = new MultiplyRight(ctx, otherId, numLeftFactors);
    return mult.multiply(inputs);
  }
  
  @Test
  public void testSingleMult() throws Exception {
    try {
      // define parties
      List<Integer> partyIds = Arrays.asList(1, 2);
      // set up runtime environment and get contexts
      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);

      MascotContext partyOneCtx = contexts.get(1);
      MascotContext partyTwoCtx = contexts.get(2);

      // left parties input (can be multiple)
      FieldElement leftInput =
          new FieldElement(12, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
      List<List<FieldElement>> leftInputs = Arrays.asList(Arrays.asList(leftInput));
      
      // single right party input element
      FieldElement rightInput =
          new FieldElement(7, partyOneCtx.getModulus(), partyTwoCtx.getkBitLength());
      List<FieldElement> rightInputs = Arrays.asList(rightInput);
      
      // define task each party will run
      Callable<List<List<FieldElement>>> partyOneTask = () -> runLeftMult(partyOneCtx, 2, leftInputs);
      Callable<List<List<FieldElement>>> partyTwoTask =
          () -> runRightMult(partyTwoCtx, 1, rightInputs, 1);

      // run tasks and get ordered list of results
      List<List<List<FieldElement>>> results =
          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));

      // get per party results
      List<List<FieldElement>> leftResults = results.get(0);
      List<List<FieldElement>> rightResults = results.get(1);

      FieldElement left = leftResults.get(0).get(0);
      FieldElement right = rightResults.get(0).get(0);
      
      FieldElement actual = left.add(right);
      FieldElement expected = leftInput.multiply(rightInput);
      
      assertEquals(expected, actual);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      throw new Exception("test failed");
    }
  }

//  @Test
//  public void testMultipleLeftFactorsMult() {
//    try {
//      // define parties
//      List<Integer> partyIds = Arrays.asList(1, 2);
//      
//      // set up runtime environment and get contexts
//      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);
//
//      // get context for each party
//      MascotContext partyOneCtx = contexts.get(1);
//      MascotContext partyTwoCtx = contexts.get(2);
//
//      // left parties input (can be multiple)
//      FieldElement leftInputOne =
//          new FieldElement(12, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
//      FieldElement leftInputTwo =
//          new FieldElement(71, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
//      FieldElement leftInputThree =
//          new FieldElement(11, partyOneCtx.getModulus(), partyOneCtx.getkBitLength());
//      List<FieldElement> leftInputs = Arrays.asList(leftInputOne, leftInputTwo, leftInputThree);
//
//      // single right party input element
//      FieldElement rightInput =
//          new FieldElement(7, partyOneCtx.getModulus(), partyTwoCtx.getkBitLength());
//
//      // define task each party will run
//      Callable<List<FieldElement>> partyOneTask = () -> runLeftMult(partyOneCtx, 2, leftInputs);
//      Callable<List<FieldElement>> partyTwoTask =
//          () -> runRightMult(partyTwoCtx, 1, rightInput, leftInputs.size());
//
//      // run tasks and get ordered list of results
//      List<List<FieldElement>> results =
//          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
//
//      // get per party results
//      List<FieldElement> leftResults = results.get(0);
//      List<FieldElement> rightResults = results.get(1);
//
//      assertEquals(leftResults.size(), leftInputs.size());
//      assertEquals(leftResults.size(), rightResults.size());
//      for (int i = 0; i < leftResults.size(); i++) {        
//        // result should be additive share of product of inputs
//        FieldElement expected = leftInputs.get(i).multiply(rightInput);
//        FieldElement actual = leftResults.get(i).add(rightResults.get(i));
//        assertEquals(expected, actual);
//      }
//    } catch (Exception e) {
//      // TODO: handle exception
//      e.printStackTrace();
//    }
//  }
//  
//  @Test
//  public void testSwappedRolesMult() {
//    try {
//      // define parties
//      List<Integer> partyIds = Arrays.asList(1, 2);
//      
//      // set up runtime environment and get contexts
//      Map<Integer, MascotContext> contexts = testRuntime.initializeContexts(partyIds);
//
//      // get context for each party
//      MascotContext partyOneCtx = contexts.get(1);
//      MascotContext partyTwoCtx = contexts.get(2);
//
//      BigInteger modulus = partyOneCtx.getModulus();
//      int kBitLength = partyOneCtx.getkBitLength();
//      
//      // left parties input (can be multiple)
//      FieldElement leftInputOne =
//          new FieldElement(12, modulus, kBitLength);
//      FieldElement leftInputTwo =
//          new FieldElement(71, modulus, kBitLength);
//      FieldElement leftInputThree =
//          new FieldElement(11, modulus, kBitLength);
//      List<FieldElement> leftInputs = Arrays.asList(leftInputOne, leftInputTwo, leftInputThree);
//
//      // single right party input element
//      FieldElement rightInput =
//          new FieldElement(7, modulus, kBitLength);
//
//      // note swapped roles
//      Callable<List<FieldElement>> partyOneTask = () -> runRightMult(partyOneCtx, 2, rightInput, leftInputs.size());
//      Callable<List<FieldElement>> partyTwoTask =
//          () -> runLeftMult(partyTwoCtx, 1, leftInputs);
//
//      // run tasks and get ordered list of results
//      List<List<FieldElement>> results =
//          testRuntime.runPerPartyTasks(Arrays.asList(partyOneTask, partyTwoTask));
//
//      // get per party results
//      List<FieldElement> leftResults = results.get(0);
//      List<FieldElement> rightResults = results.get(1);
//
//      assertEquals(leftResults.size(), leftInputs.size());
//      assertEquals(leftResults.size(), rightResults.size());
//      for (int i = 0; i < leftResults.size(); i++) {        
//        // result should be additive share of product of inputs
//        FieldElement expected = leftInputs.get(i).multiply(rightInput);
//        FieldElement actual = leftResults.get(i).add(rightResults.get(i));
//        assertEquals(expected, actual);
//      }
//    } catch (Exception e) {
//      // TODO: handle exception
//      e.printStackTrace();
//    }
//  }
  
}
