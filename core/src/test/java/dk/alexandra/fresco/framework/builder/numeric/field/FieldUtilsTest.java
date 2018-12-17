package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Test;

public final class FieldUtilsTest {

  private BigIntegerFieldDefinition bigIntegerFieldDefinition =
      new BigIntegerFieldDefinition("340282366920938463463374607431768211283");
  private MersennePrimeFieldDefinition mersennePrimeFieldDefinition =
      new MersennePrimeFieldDefinition(128, 173);
  private byte[] bytes = new byte[]{
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 127,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -87,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 82
  };

  private List<FieldElement> getElements(FieldDefinition definition) {
    return Arrays.asList(
        definition.createElement(0),
        definition.createElement(definition.getModulus().shiftRight(1)),
        definition.createElement(definition.getModulus().subtract(BigInteger.ONE))
    );
  }

  @Test
  public void convertToBitVector() {
    byte[] bytes = {63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -44};

    BigInteger value = mersennePrimeFieldDefinition.getModulus().shiftRight(2);
    FieldElement fieldElement = mersennePrimeFieldDefinition.createElement(value);
    StrictBitVector vector = mersennePrimeFieldDefinition.convertToBitVector(fieldElement);
    StrictBitVector expected = new StrictBitVector(bytes);
    assertThat(vector, Is.is(expected));

    value = bigIntegerFieldDefinition.getModulus().shiftRight(2);
    fieldElement = bigIntegerFieldDefinition.createElement(value);
    vector = bigIntegerFieldDefinition.convertToBitVector(fieldElement);
    expected = new StrictBitVector(bytes);
    assertThat(vector, Is.is(expected));
  }

  @Test
  public void serializeList() {
    List<FieldElement> elements = getElements(mersennePrimeFieldDefinition);
    byte[] result = mersennePrimeFieldDefinition.serialize(elements);
    assertThat(result, Is.is(bytes));

    elements = getElements(bigIntegerFieldDefinition);
    result = bigIntegerFieldDefinition.serialize(elements);
    assertThat(result, Is.is(bytes));
  }

  @Test
  public void deserializeList() {
    List<FieldElement> result = mersennePrimeFieldDefinition.deserializeList(bytes);
    assertThat(toBigIntegers(result, MersennePrimeFieldElement::extractValue), Is.is(
        toBigIntegers(getElements(mersennePrimeFieldDefinition),
            MersennePrimeFieldElement::extractValue)));

    result = bigIntegerFieldDefinition.deserializeList(bytes);
    assertThat(toBigIntegers(result, BigIntegerFieldElement::extractValue), Is.is(
        toBigIntegers(getElements(bigIntegerFieldDefinition),
            BigIntegerFieldElement::extractValue)));
  }

  private List<BigInteger> toBigIntegers(List<FieldElement> elements,
      Function<FieldElement, BigInteger> converter) {
    return elements.stream().map(converter).collect(Collectors.toList());
  }
}
