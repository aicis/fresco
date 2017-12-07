package dk.alexandra.fresco.tools.mascot.cope;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.mock.MockMascotContext;
import dk.alexandra.fresco.tools.mascot.mult.FailedMultException;
import dk.alexandra.fresco.tools.mascot.mult.MaliciousMultException;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;

public class TestCopeSigner {

  // negative tests

  public void copeSignerExceptionTest(MultiplyLeft multiplier, String expectedMessage,
      Class<? extends Exception> expectedClass) throws Exception {
    CopeSigner inputter = new CopeSigner(new MockMascotContext(), 1, null);
    Field field = CopeSigner.class.getDeclaredField("multiplier");
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
  public void testMaliciousSignerInit() throws Exception {
    MultiplyLeft mockMultiplier = new MultiplyLeft(new MockMascotContext(), 1) {
      @Override
      public List<StrictBitVector> generateSeeds(List<FieldElement> leftFactors)
          throws MaliciousMultException, FailedMultException {
        throw new MaliciousMultException();
      }
    };
    String expectedMessage = "Malicious failure during initialization";
    copeSignerExceptionTest(mockMultiplier, expectedMessage, MaliciousCopeException.class);
  }

  @Test
  public void testFailedSignerInit() throws Exception {
    MultiplyLeft mockMultiplier = new MultiplyLeft(new MockMascotContext(), 1) {
      @Override
      public List<StrictBitVector> generateSeeds(List<FieldElement> leftFactors)
          throws MaliciousMultException, FailedMultException {
        throw new FailedMultException();
      }
    };
    String expectedMessage = "Non-malicious failure during initialization";
    copeSignerExceptionTest(mockMultiplier, expectedMessage, FailedCopeException.class);
  }

}
