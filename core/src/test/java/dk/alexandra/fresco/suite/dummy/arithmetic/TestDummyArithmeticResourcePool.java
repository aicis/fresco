package dk.alexandra.fresco.suite.dummy.arithmetic;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.math.BigInteger;
import org.junit.Test;

public class TestDummyArithmeticResourcePool {

  @Test
  public void testDefaultConstructor() throws Exception {
    DummyArithmeticResourcePool pool = new DummyArithmeticResourcePoolImpl(1, 1);
    BigInteger expectedModulus = ModulusFinder.findSuitableModulus(128);
    assertEquals(expectedModulus, pool.getModulus());
  }

}
