package dk.alexandra.fresco.framework.builder.numeric.field;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Test;

public class MersennePrimeModulusTest {

  @Test
  public void equals() {
    MersennePrimeModulus first = new MersennePrimeModulus(512, 569);
    MersennePrimeModulus firstAgain = new MersennePrimeModulus(512, 569);
    MersennePrimeModulus differentConstant = new MersennePrimeModulus(512, 629);
    MersennePrimeModulus invalidMersenneWithDifferentBitLength =
        new MersennePrimeModulus(256, 569);

    assertTrue(first.equals(first));
    assertTrue(first.equals(firstAgain));
    assertFalse(first.equals(differentConstant));
    assertFalse(first.equals(""));
    assertFalse(first.equals(null));
    assertFalse(first.equals(invalidMersenneWithDifferentBitLength));
  }

  @Test
  public void toStringTest() {
    MersennePrimeModulus prime = new MersennePrimeModulus(160, 47);
    assertThat(
        prime.toString(),
        CoreMatchers.containsString("1461501637330902918203684832716283019655932542929"));
  }

  @Test
  public void modulus() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(160, 47);
    Random random = new Random(0xFFAA115599L);
    for (int i = 0; i < 50; i++) {
      BigInteger value = new BigInteger(180 + random.nextInt(50), random);
      BigInteger mod = modulus.mod(value);
      assertThat(mod, Is.is(value.mod(modulus.getBigInteger())));
    }
  }
}