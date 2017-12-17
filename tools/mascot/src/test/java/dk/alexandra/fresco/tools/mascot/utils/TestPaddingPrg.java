package dk.alexandra.fresco.tools.mascot.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

// TODO a test that asserts the that result looks "random" as a sanity check?
public class TestPaddingPrg {

  private final BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
  private final int modBitLength = 128;

  @Test
  public void testGetNextProducesFieldElement() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes, 256);
    FieldElementPrg prg = new FieldElementPrgImpl(seed);
    FieldElement el = prg.getNext(modulus, modBitLength);
    assertEquals(modulus, el.getModulus());
    assertEquals(modBitLength, el.getBitLength());
  }

  @Test
  public void testGetNextProducesDifferentFieldElements() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes, 256);
    FieldElementPrg prg = new FieldElementPrgImpl(seed);
    FieldElement elOne = prg.getNext(modulus, modBitLength);
    FieldElement elTwo = prg.getNext(modulus, modBitLength);
    // not equals, without actually using equals
    assertFalse(elOne.subtract(elTwo).isZero());
  }

  @Test
  public void testSameSeedsProduceSameFieldElements() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes, 256);
    StrictBitVector seedOther = new StrictBitVector(seedBytes, 256);
    FieldElementPrg prgOne = new FieldElementPrgImpl(seed);
    FieldElementPrg prgTwo = new FieldElementPrgImpl(seedOther);
    FieldElement elOne = prgOne.getNext(modulus, modBitLength);
    FieldElement elTwo = prgTwo.getNext(modulus, modBitLength);
    CustomAsserts.assertEquals(elOne, elTwo);
  }

  @Test
  public void testDifferentSeedsProduceDifferentFieldElements() {
    byte[] seedBytesOne = new byte[32];
    new Random().nextBytes(seedBytesOne);
    seedBytesOne[0] = (byte) 0x01;
    StrictBitVector seedOne = new StrictBitVector(seedBytesOne, 256);
    FieldElementPrg prgOne = new FieldElementPrgImpl(seedOne);

    // make sure other seed is different
    byte[] seedBytesTwo = Arrays.copyOf(seedBytesOne, 32);
    seedBytesTwo[0] = (byte) 0x02;
    StrictBitVector seedTwo = new StrictBitVector(seedBytesTwo, 256);
    FieldElementPrg prgTwo = new FieldElementPrgImpl(seedTwo);

    FieldElement elOne = prgOne.getNext(modulus, modBitLength);
    FieldElement elTwo = prgTwo.getNext(modulus, modBitLength);
    assertNotEquals(elOne, elTwo);
  }

}
