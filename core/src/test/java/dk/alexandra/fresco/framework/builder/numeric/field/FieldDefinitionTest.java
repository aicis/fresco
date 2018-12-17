package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hamcrest.core.Is;
import org.junit.Test;

public final class FieldDefinitionTest {

  private byte[] bytes = new byte[]{
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -87,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 82
  };

  private List<FieldElement> getElements(FieldDefinition definition) {
    return Arrays.asList(
        definition.createElement(0),
        definition.createElement(definition.getModulus().shiftRight(1)),
        definition.createElement(definition.getModulus()).subtract(definition.createElement(1))
    );
  }

  private void testDefinition(
      BiConsumer<FieldDefinition, Function<FieldElement, BigInteger>> test) {
    test.accept(new BigIntegerFieldDefinition("340282366920938463463374607431768211283"),
        BigIntegerFieldElement::extractValue);
    test.accept(new MersennePrimeFieldDefinition(128, 173),
        MersennePrimeFieldElement::extractValue);
  }

  private void testDefinition(Consumer<FieldDefinition> test) {
    test.accept(new BigIntegerFieldDefinition("340282366920938463463374607431768211283"));
    test.accept(new MersennePrimeFieldDefinition(128, 173));
  }

  private List<BigInteger> toBigIntegers(List<FieldElement> elements,
      Function<FieldElement, BigInteger> converter) {
    return elements.stream().map(converter).collect(Collectors.toList());
  }

  @Test
  public void bitLength() {
    testDefinition(definition -> assertThat(definition.getBitLength(), Is.is(128)));
  }

  @Test
  public void convertToBitVector() {
    byte[] bytes = {63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -44};
    testDefinition(definition -> {
      BigInteger value = definition.getModulus().shiftRight(2);
      FieldElement fieldElement = definition.createElement(value);
      StrictBitVector vector = definition.convertToBitVector(fieldElement);
      StrictBitVector expected = new StrictBitVector(bytes);
      assertThat(vector, Is.is(expected));
    });
  }

  @Test
  public void serializeList() {
    testDefinition(definition -> {
      List<FieldElement> elements = getElements(definition);
      byte[] result = definition.serialize(elements);
      assertThat(result, Is.is(bytes));
    });
  }

  @Test
  public void deserializeList() {
    testDefinition((definition, converter) -> {
      List<FieldElement> result = definition.deserializeList(bytes);
      assertThat(toBigIntegers(result, converter),
          Is.is(toBigIntegers(getElements(definition), converter)));
    });
  }

  @Test
  public void serialize() {
    testDefinition((definition, converter) -> {
      FieldElement element = definition.createElement(0);
      byte[] result = definition.serialize(element);
      assertThat(result, Is.is(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
    });
  }

  @Test
  public void deserialize() {
    testDefinition((definition, converter) -> {
      FieldElement result = definition
          .deserialize(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
      assertThat(converter.apply(result), Is.is(BigInteger.ZERO));
    });
  }

  @Test
  public void serializeAndList() {
    testDefinition((definition, converter) -> {
      List<FieldElement> elements = getElements(definition);
      for (FieldElement element : elements) {
        byte[] result = definition.serialize(element);
        byte[] serialize = definition.serialize(Collections.singletonList(element));
        assertThat(result, Is.is(serialize));
      }
    });
  }

  @Test
  public void deserializeStupidList() {
    testDefinition((definition, converter) -> {
      List<FieldElement> result = definition.deserializeList(Arrays.asList(
          new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
          new byte[]{127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -87},
          new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 82}
      ));
      assertThat(toBigIntegers(result, converter),
          Is.is(toBigIntegers(getElements(definition), converter)));
    });
  }
}
