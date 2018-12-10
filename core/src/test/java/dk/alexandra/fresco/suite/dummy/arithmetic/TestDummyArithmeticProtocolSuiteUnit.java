package dk.alexandra.fresco.suite.dummy.arithmetic;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.lang.reflect.Field;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuiteUnit {

  @Test
  public void testDefaultConstructor() throws Exception {
    DummyArithmeticProtocolSuite suite = new DummyArithmeticProtocolSuite(
        new FieldDefinitionBigInteger(
            new ModulusBigInteger(ModulusFinder.findSuitableModulus(128))), 32, 4);
    Modulus expectedModulus = new ModulusBigInteger(ModulusFinder.findSuitableModulus(128));
    Field field = DummyArithmeticProtocolSuite.class.getDeclaredField("fieldDefinition");
    field.setAccessible(true);
    Modulus actualModulus = ((FieldDefinition) field.get(suite)).getModulus();
    assertEquals(expectedModulus, actualModulus);
    int expectedMaxBitLength = 32;
    Field otherField = DummyArithmeticProtocolSuite.class.getDeclaredField("maxBitLength");
    otherField.setAccessible(true);
    int actualMaxBitLength = (int) otherField.get(suite);
    assertEquals(expectedMaxBitLength, actualMaxBitLength);
  }
}
