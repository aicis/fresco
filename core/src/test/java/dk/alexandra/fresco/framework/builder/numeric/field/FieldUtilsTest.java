package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.core.Is;
import org.junit.Test;

public final class FieldUtilsTest {

  private BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(
      ModulusFinder.findSuitableModulus(8));
  private List<FieldElement> elements = Arrays.asList(
      definition.createElement(0),
      definition.createElement(definition.getModulus().shiftRight(1)),
      definition.createElement(definition.getModulus().subtract(BigInteger.ONE))
  );
  private byte[] bytes = new byte[]{0, 125, -6};

  @Test
  public void convertToBitVector() {
    BigInteger value = definition.getModulus().shiftRight(2);
    FieldElement fieldElement = definition.createElement(value);
    StrictBitVector vector = definition.convertToBitVector(fieldElement);
    StrictBitVector expected = new StrictBitVector(new byte[]{0, 0, 0, 0, 0, 0, 0, 62});
    assertThat(vector, Is.is(expected));
  }

  @Test
  public void serializeList() {
    byte[] result = definition.serialize(elements);
    assertThat(result, Is.is(bytes));
  }

  @Test
  public void deserializeList() {
    List<FieldElement> result = definition.deserializeList(bytes);
    assertThat(result, Is.is(elements));
  }
}
