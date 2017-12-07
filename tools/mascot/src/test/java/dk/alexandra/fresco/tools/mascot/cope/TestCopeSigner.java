package dk.alexandra.fresco.tools.mascot.cope;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.tools.mascot.mock.MockMascotContext;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyLeft;
import java.lang.reflect.Field;

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
}
