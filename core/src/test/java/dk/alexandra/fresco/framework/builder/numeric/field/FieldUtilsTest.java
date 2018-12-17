package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.core.Is;
import org.junit.Test;

public final class FieldUtilsTest {

  private BigIntegerFieldDefinition bigIntegerFieldDefinition =
      new BigIntegerFieldDefinition("340282366920938463463374607431768211283");
  private MersennePrimeFieldDefinition mersennePrimeFieldDefinition =
      new MersennePrimeFieldDefinition(128, 173);
  private byte[] bytes = new byte[]{0, 125, -6};

  private List<FieldElement> getElements(FieldDefinition definition) {
    return Arrays.asList(
        definition.createElement(0),
        definition.createElement(definition.getModulus().shiftRight(1)),
        definition.createElement(definition.getModulus().subtract(BigInteger.ONE))
    );
  }

  @Test
  public void convertToBitVector() {
    BigInteger value = mersennePrimeFieldDefinition.getModulus().shiftRight(2);
    FieldElement fieldElement = mersennePrimeFieldDefinition.createElement(value);
    StrictBitVector vector = mersennePrimeFieldDefinition.convertToBitVector(fieldElement);
    StrictBitVector expected = new StrictBitVector(new byte[]{0, 0, 0, 0, 0, 0, 0, 62});
    assertThat(vector, Is.is(expected));

    value = bigIntegerFieldDefinition.getModulus().shiftRight(2);
    fieldElement = bigIntegerFieldDefinition.createElement(value);
    vector = bigIntegerFieldDefinition.convertToBitVector(fieldElement);
    expected = new StrictBitVector(new byte[]{0, 0, 0, 0, 0, 0, 0, 62});
    assertThat(vector, Is.is(expected));
  }

  @Test
  public void serializeList() {
    byte[] result = mersennePrimeFieldDefinition
        .serialize(getElements(mersennePrimeFieldDefinition));
    assertThat(result, Is.is(bytes));

    result = bigIntegerFieldDefinition.serialize(getElements(bigIntegerFieldDefinition));
    assertThat(result, Is.is(bytes));
  }

  @Test
  public void deserializeList() {
    List<FieldElement> result = mersennePrimeFieldDefinition.deserializeList(bytes);
    assertThat(result, Is.is(getElements(mersennePrimeFieldDefinition)));

    result = bigIntegerFieldDefinition.deserializeList(bytes);
    assertThat(result, Is.is(getElements(bigIntegerFieldDefinition)));
  }
}
