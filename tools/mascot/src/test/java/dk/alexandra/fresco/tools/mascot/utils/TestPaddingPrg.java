package dk.alexandra.fresco.tools.mascot.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

public class TestPaddingPrg {

  private final BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");

  @Test
  public void testGetNextProducesFieldElement() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes);
    FieldElementPrg prg = new FieldElementPrgImpl(seed);
    FieldElement el = prg.getNext(modulus);
    assertEquals(modulus, el.getModulus());
    int modBitLength = 128;
    assertEquals(modBitLength, el.getBitLength());
  }

  @Test
  public void testGetNextProducesNonZeroResult() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes);
    FieldElementPrg prg = new FieldElementPrgImpl(seed);
    FieldElement el = prg.getNext(modulus);
    assertFalse(el.isZero());
  }

  @Test
  public void testGetNextProducesDifferentFieldElements() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes);
    FieldElementPrg prg = new FieldElementPrgImpl(seed);
    FieldElement elOne = prg.getNext(modulus);
    FieldElement elTwo = prg.getNext(modulus);
    // not equals, without actually using equals
    assertFalse(elOne.subtract(elTwo).isZero());
  }

  @Test
  public void testSameSeedsProduceSameFieldElements() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes);
    StrictBitVector seedOther = new StrictBitVector(seedBytes);
    FieldElementPrg prgOne = new FieldElementPrgImpl(seed);
    FieldElementPrg prgTwo = new FieldElementPrgImpl(seedOther);
    FieldElement elOne = prgOne.getNext(modulus);
    FieldElement elTwo = prgTwo.getNext(modulus);
    CustomAsserts.assertEquals(elOne, elTwo);
  }

  @Test
  public void testDifferentSeedsProduceDifferentFieldElements() {
    byte[] seedBytesOne = new byte[32];
    new Random().nextBytes(seedBytesOne);
    seedBytesOne[0] = (byte) 0x01;
    StrictBitVector seedOne = new StrictBitVector(seedBytesOne);
    FieldElementPrg prgOne = new FieldElementPrgImpl(seedOne);

    // make sure other seed is different
    byte[] seedBytesTwo = Arrays.copyOf(seedBytesOne, 32);
    seedBytesTwo[0] = (byte) 0x02;
    StrictBitVector seedTwo = new StrictBitVector(seedBytesTwo);
    FieldElementPrg prgTwo = new FieldElementPrgImpl(seedTwo);

    FieldElement elOne = prgOne.getNext(modulus);
    FieldElement elTwo = prgTwo.getNext(modulus);
    assertNotEquals(elOne, elTwo);
  }

}
