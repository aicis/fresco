package dk.alexandra.fresco.tools.mascot.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class TestPaddingPrg {

  private final BigInteger modulus = new BigInteger("340282366920938463463374607431768211297");
  private final int modBitLength = 128;

  @Test
  public void testPadShortSeed() {
    byte[] seedBytes = {0x01, 0x02, 0x03};
    StrictBitVector seed = new StrictBitVector(seedBytes, 24);
    byte[] expecteds = new byte[32];
    expecteds[0] = 0x01;
    expecteds[1] = 0x02;
    expecteds[2] = 0x03;
    byte[] actuals = PaddingPrg.toBytes(seed);
    assertArrayEquals(expecteds, actuals);
  }

  @Test
  public void testExactLengthSeed() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes, 256);
    byte[] actuals = PaddingPrg.toBytes(seed);
    assertArrayEquals(seedBytes, actuals);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testThrowLongSeed() {
    byte[] seedBytes = new byte[40];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes, 320);
    PaddingPrg.toBytes(seed);
  }

  @Test
  public void testGetNextProducesFieldElement() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes, 256);
    FieldElementPrg prg = new PaddingPrg(seed);
    FieldElement el = prg.getNext(modulus, modBitLength);
    assertEquals(modulus, el.getModulus());
    assertEquals(modBitLength, el.getBitLength());
  }

  @Test
  public void testGetNextProducesDifferentFieldElements() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes, 256);
    FieldElementPrg prg = new PaddingPrg(seed);
    FieldElement elOne = prg.getNext(modulus, modBitLength);
    FieldElement elTwo = prg.getNext(modulus, modBitLength);
    assertNotEquals(elOne, elTwo);
  }

  @Test
  public void testSameSeedsProduceSameFieldElements() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes, 256);
    StrictBitVector seedOther = new StrictBitVector(seedBytes, 256);
    FieldElementPrg prgOne = new PaddingPrg(seed);
    FieldElementPrg prgTwo = new PaddingPrg(seedOther);
    FieldElement elOne = prgOne.getNext(modulus, modBitLength);
    FieldElement elTwo = prgTwo.getNext(modulus, modBitLength);
    assertEquals(elOne, elTwo);
  }

  @Test
  public void testDifferentSeedsProduceDifferentFieldElements() {
    byte[] seedBytesOne = new byte[32];
    new Random().nextBytes(seedBytesOne);
    seedBytesOne[0] = (byte) 0x01;
    StrictBitVector seedOne = new StrictBitVector(seedBytesOne, 256);
    FieldElementPrg prgOne = new PaddingPrg(seedOne);

    // make sure other seed is different
    byte[] seedBytesTwo = Arrays.copyOf(seedBytesOne, 32);
    seedBytesTwo[0] = (byte) 0x02;
    StrictBitVector seedTwo = new StrictBitVector(seedBytesTwo, 256);
    FieldElementPrg prgTwo = new PaddingPrg(seedTwo);

    FieldElement elOne = prgOne.getNext(modulus, modBitLength);
    FieldElement elTwo = prgTwo.getNext(modulus, modBitLength);
    assertNotEquals(elOne, elTwo);
  }

  // TODO a test that asserts the that result looks "random" as a sanity check?

}
