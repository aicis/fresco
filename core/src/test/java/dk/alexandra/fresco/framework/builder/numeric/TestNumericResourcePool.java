package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import java.math.BigInteger;
import org.junit.Test;

public class TestNumericResourcePool {

  private final BigIntegerFieldDefinition fieldDefinition = new BigIntegerFieldDefinition("251");

  @Test
  public void testConvertRepresentationLessThanHalf() {
    BigInteger actual = fieldDefinition.convertToSigned(BigInteger.TEN);
    assertEquals(BigInteger.TEN, actual);
  }

  @Test
  public void testConvertRepresentationGreaterThanHalf() {
    BigInteger actual = fieldDefinition.convertToSigned(new BigInteger("200"));
    assertEquals(new BigInteger("200").subtract(fieldDefinition.getModulus()), actual);
  }

  @Test
  public void testConvertRepresentationEqualsHalf() {
    BigInteger modulusHalf = fieldDefinition.getModulus().shiftRight(1);
    BigInteger actual = fieldDefinition.convertToSigned(modulusHalf);
    assertEquals(modulusHalf, actual);
  }
}
