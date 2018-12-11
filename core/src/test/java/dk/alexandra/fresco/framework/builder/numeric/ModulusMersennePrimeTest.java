package dk.alexandra.fresco.framework.builder.numeric;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class ModulusMersennePrimeTest {

  @Test
  public void equals() {
    ModulusMersennePrime first = new ModulusMersennePrime(512, 569);
    ModulusMersennePrime firstAgain = new ModulusMersennePrime(512, 569);
    ModulusMersennePrime second = new ModulusMersennePrime(512, 629);
    ModulusMersennePrime invalidMersenne = new ModulusMersennePrime(256, 629);

    assertTrue(first.equals(first));
    assertTrue(first.equals(firstAgain));
    assertFalse(first.equals(second));
    assertFalse(first.equals(""));
    assertFalse(first.equals(null));
    assertFalse(first.equals(invalidMersenne));
  }

  @Test
  public void toStringTest() {
    ModulusMersennePrime prime = new ModulusMersennePrime(160, 47);
    assertThat(
        prime.toString(),
        CoreMatchers.containsString("1461501637330902918203684832716283019655932542929"));
  }
}