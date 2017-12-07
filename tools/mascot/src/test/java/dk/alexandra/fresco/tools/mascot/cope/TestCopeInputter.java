package dk.alexandra.fresco.tools.mascot.cope;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.tools.mascot.mock.MockMascotContext;
import dk.alexandra.fresco.tools.mascot.mult.MultiplyRight;
import java.lang.reflect.Field;

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
}
