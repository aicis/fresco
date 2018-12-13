package dk.alexandra.fresco.framework.builder.numeric.field;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class MersennePrimeFieldElementTest {

  @Test
  public void equals() {
    MersennePrimeModulus firstPrime = new MersennePrimeModulus(160, 47);
    MersennePrimeModulus secondPrime = new MersennePrimeModulus(160, 57);
    FieldElement firstElement =
        MersennePrimeFieldElement.create(BigInteger.valueOf(27), firstPrime);
    FieldElement firstElementAgain =
        MersennePrimeFieldElement.create(
            firstPrime.getBigInteger().add(BigInteger.valueOf(27)), firstPrime);
    FieldElement differentValue =
        MersennePrimeFieldElement.create(BigInteger.valueOf(28), firstPrime);
    FieldElement differentPrime =
        MersennePrimeFieldElement.create(BigInteger.valueOf(27), secondPrime);

    assertTrue(firstElement.equals(firstElementAgain));
    assertFalse(firstElement.equals(differentValue));
    assertFalse(firstElement.equals(differentPrime));

    assertTrue(firstElement.equals(firstElement));
    assertFalse(firstElement.equals(""));
    assertFalse(firstElement.equals(null));
  }

  @Test
  public void toStringTest() {
    MersennePrimeModulus firstPrime = new MersennePrimeModulus(160, 47);
    FieldElement element = MersennePrimeFieldElement.create(BigInteger.valueOf(7854), firstPrime);

    assertThat(element.toString(), StringContains.containsString("7854"));
  }
}