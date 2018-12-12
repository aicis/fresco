package dk.alexandra.fresco.framework.builder.numeric;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Test;

public class ModulusMersennePrimeTest {

  @Test
  public void equals() {
    ModulusMersennePrime first = new ModulusMersennePrime(512, 569);
    ModulusMersennePrime firstAgain = new ModulusMersennePrime(512, 569);
    ModulusMersennePrime differentConstant = new ModulusMersennePrime(512, 629);
    ModulusMersennePrime invalidMersenneWithDifferentBitLength =
        new ModulusMersennePrime(256, 569);

    assertTrue(first.equals(first));
    assertTrue(first.equals(firstAgain));
    assertFalse(first.equals(differentConstant));
    assertFalse(first.equals(""));
    assertFalse(first.equals(null));
    assertFalse(first.equals(invalidMersenneWithDifferentBitLength));
  }

  @Test
  public void toStringTest() {
    ModulusMersennePrime prime = new ModulusMersennePrime(160, 47);
    assertThat(
        prime.toString(),
        CoreMatchers.containsString("1461501637330902918203684832716283019655932542929"));
  }

  @Test
  public void modulus() {
    ModulusMersennePrime modulus = new ModulusMersennePrime(160, 47);
    Random random = new Random(0xFFAA115599L);
    for (int i = 0; i < 50; i++) {
      BigInteger value = new BigInteger(180 + random.nextInt(50), random);
      BigInteger mod = modulus.mod(value);
      assertThat(mod, Is.is(value.mod(modulus.getBigInteger())));
    }
  }
}