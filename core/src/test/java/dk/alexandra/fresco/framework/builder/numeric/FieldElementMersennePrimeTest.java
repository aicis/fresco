package dk.alexandra.fresco.framework.builder.numeric;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class FieldElementMersennePrimeTest {

  @Test
  public void equals() {
    ModulusMersennePrime firstPrime = new ModulusMersennePrime(160, 47);
    ModulusMersennePrime secondPrime = new ModulusMersennePrime(160, 57);
    FieldElement firstElement =
        FieldElementMersennePrime.create(BigInteger.valueOf(27), firstPrime);
    FieldElement firstElementAgain =
        FieldElementMersennePrime.create(
            firstPrime.getBigInteger().add(BigInteger.valueOf(27)), firstPrime);
    FieldElement differentValue =
        FieldElementMersennePrime.create(BigInteger.valueOf(28), firstPrime);
    FieldElement differentPrime =
        FieldElementMersennePrime.create(BigInteger.valueOf(27), secondPrime);

    assertTrue(firstElement.equals(firstElementAgain));
    assertFalse(firstElement.equals(differentValue));
    assertFalse(firstElement.equals(differentPrime));

    assertTrue(firstElement.equals(firstElement));
    assertFalse(firstElement.equals(""));
    assertFalse(firstElement.equals(null));
  }

  @Test
  public void toStringTest() {
    ModulusMersennePrime firstPrime = new ModulusMersennePrime(160, 47);
    FieldElement element = FieldElementMersennePrime.create(BigInteger.valueOf(7854), firstPrime);

    assertThat(element.toString(), StringContains.containsString("7854"));
  }
}