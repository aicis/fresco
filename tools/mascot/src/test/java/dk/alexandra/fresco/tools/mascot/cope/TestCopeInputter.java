package dk.alexandra.fresco.tools.mascot.cope;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.mock.MockMascotContext;
import dk.alexandra.fresco.tools.mascot.mult.FailedMultException;
import dk.alexandra.fresco.tools.mascot.mult.MaliciousMultException;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;

public class TestCopeInputter {

  // negative tests

  public void copeInputterExceptionTest(MultiplyRight multiplier, String expectedMessage,
      Class<? extends Exception> expectedClass) throws Exception {
    CopeInputter inputter = new CopeInputter(new MockMascotContext(), 1);
    Field field = CopeInputter.class.getDeclaredField("multiplier");
    field.setAccessible(true);
    field.set(inputter, multiplier);

    boolean thrown = false;
    Exception actual = null;
    try {
      inputter.initialize();
    } catch (Exception e) {
      thrown = true;
      actual = e;
    }

    assertEquals(thrown, true);
    assertEquals(actual.getClass(), expectedClass);
    assertEquals(actual.getMessage(), expectedMessage);
  }

  @Test
  public void testFailedInputterInit() throws Exception {
    MultiplyRight mockMultiplier = new MultiplyRight(new MockMascotContext(), 1) {
      @Override
      public List<Pair<StrictBitVector, StrictBitVector>> generateSeeds(int numMults)
          throws MaliciousMultException, FailedMultException {
        throw new FailedMultException();
      }
    };
    String expectedMessage = "Non-malicious failure during initialization";
    copeInputterExceptionTest(mockMultiplier, expectedMessage, FailedCopeException.class);
  }

  @Test
  public void testMaliciousInputterInit() throws Exception {
    MultiplyRight mockMultiplier = new MultiplyRight(new MockMascotContext(), 1) {
      @Override
      public List<Pair<StrictBitVector, StrictBitVector>> generateSeeds(int numMults)
          throws MaliciousMultException, FailedMultException {
        throw new MaliciousMultException();
      }
    };
    String expectedMessage = "Malicious failure during initialization";
    copeInputterExceptionTest(mockMultiplier, expectedMessage, MaliciousCopeException.class);
  }

}
