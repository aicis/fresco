package dk.alexandra.fresco.framework.builder.numeric;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;

public class TestNumericResourcePool {

  private final FieldDefinitionBigInteger fieldDefinition = new FieldDefinitionBigInteger(
      new ModulusBigInteger("251"));

  @Test
  public void testConvertRepresentationLessThanHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(fieldDefinition);
    BigInteger actual = convertRepresentation(pool, BigInteger.TEN);
    assertEquals(BigInteger.TEN, actual);
  }

  private BigInteger convertRepresentation(NumericResourcePool pool, BigInteger bigInteger) {
    FieldElement value = fieldDefinition.createElement(bigInteger);
    return pool.convertRepresentation(value);
  }

  @Test
  public void testConvertRepresentationGreaterThanHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(fieldDefinition);
    BigInteger actual = convertRepresentation(pool, new BigInteger("200"));
    assertEquals(new BigInteger("200").subtract(fieldDefinition.getModulus()),
        actual);
  }

  @Test
  public void testConvertRepresentationEqualsHalf() {
    NumericResourcePool pool = new MockNumericResourcePool(fieldDefinition);
    BigInteger actual = convertRepresentation(pool, fieldDefinition.getModulusHalved());
    assertEquals(fieldDefinition.getModulusHalved(), actual);
  }

  private class MockNumericResourcePool implements NumericResourcePool {

    private final FieldDefinition fieldDefinition;

    MockNumericResourcePool(FieldDefinition fieldDefinition) {
      this.fieldDefinition = fieldDefinition;
    }

    @Override
    public int getMyId() {
      return 0;
    }

    @Override
    public int getNoOfParties() {
      return 0;
    }

    @Override
    public FieldDefinition getFieldDefinition() {
      return fieldDefinition;
    }
  }
}
