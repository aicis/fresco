package dk.alexandra.fresco.suite.dummy.arithmetic;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.lang.reflect.Field;
import java.math.BigInteger;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuiteUnit {

  @Test
  public void testDefaultConstructor() throws Exception {
    DummyArithmeticProtocolSuite suite = new DummyArithmeticProtocolSuite();
    Modulus expectedModulus = ModulusFinder.findSuitableModulus(128);
    Field field = DummyArithmeticProtocolSuite.class.getDeclaredField("modulus");
    field.setAccessible(true);
    Modulus actualModulus = (Modulus) field.get(suite);
    assertEquals(expectedModulus, actualModulus);
    int expectedMaxBitLength = 32;
    Field otherField = DummyArithmeticProtocolSuite.class.getDeclaredField("maxBitLength");
    otherField.setAccessible(true);
    int actualMaxBitLength = (int) otherField.get(suite);
    assertEquals(expectedMaxBitLength, actualMaxBitLength);
  }
}
