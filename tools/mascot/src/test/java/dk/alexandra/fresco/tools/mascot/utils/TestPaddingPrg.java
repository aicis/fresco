package dk.alexandra.fresco.tools.mascot.utils;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerModulus;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

public class TestPaddingPrg {

  private final BigInteger modulus = new BigInteger(
      "340282366920938463463374607431768211297");
  private BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(
      new BigIntegerModulus(modulus));

  @Test
  public void testGetNextProducesFieldElement() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes);
    FieldElementPrg prg = new FieldElementPrgImpl(seed, definition);
    FieldElement el = prg.getNext();
    assertNotNull(el);
  }

  @Test
  public void testGetNextProducesNonZeroResult() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes);
    FieldElementPrg prg = new FieldElementPrgImpl(seed, definition);
    FieldElement el = prg.getNext();
    BigInteger open = definition.convertRepresentation(el);
    assertNotEquals(BigInteger.ZERO, open);
  }

  @Test
  public void testGetNextProducesDifferentFieldElements() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes);
    FieldElementPrg prg = new FieldElementPrgImpl(seed, definition);
    FieldElement elOne = prg.getNext();
    FieldElement elTwo = prg.getNext();
    assertNotEquals(elOne, elTwo);
  }

  @Test
  public void testSameSeedsProduceSameFieldElements() {
    byte[] seedBytes = new byte[32];
    new Random().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(seedBytes);
    StrictBitVector seedOther = new StrictBitVector(seedBytes);
    FieldElementPrg prgOne = new FieldElementPrgImpl(seed, definition);
    FieldElementPrg prgTwo = new FieldElementPrgImpl(seedOther, definition);
    FieldElement elOne = prgOne.getNext();
    FieldElement elTwo = prgTwo.getNext();
    CustomAsserts.assertEquals(elOne, elTwo);
  }

  @Test
  public void testDifferentSeedsProduceDifferentFieldElements() {
    byte[] seedBytesOne = new byte[32];
    new Random().nextBytes(seedBytesOne);
    seedBytesOne[0] = (byte) 0x01;
    StrictBitVector seedOne = new StrictBitVector(seedBytesOne);
    FieldElementPrg prgOne = new FieldElementPrgImpl(seedOne, definition);

    // make sure other seed is different
    byte[] seedBytesTwo = Arrays.copyOf(seedBytesOne, 32);
    seedBytesTwo[0] = (byte) 0x02;
    StrictBitVector seedTwo = new StrictBitVector(seedBytesTwo);
    FieldElementPrg prgTwo = new FieldElementPrgImpl(seedTwo, definition);

    FieldElement elOne = prgOne.getNext();
    FieldElement elTwo = prgTwo.getNext();
    assertNotEquals(elOne, elTwo);
  }
}
