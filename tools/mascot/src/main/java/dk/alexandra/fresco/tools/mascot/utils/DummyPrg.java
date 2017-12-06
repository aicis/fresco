package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;
import java.util.Random;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

// this class is temporary and will go away once we have a real implementation
public class DummyPrg implements FieldElementPrg {

  private Random rand;
  private FieldElement next;

  public DummyPrg(StrictBitVector seed, BigInteger modulus, int modBitLength)
      throws IllegalArgumentException {
    if (seed.getSize() < modBitLength) {
      throw new IllegalArgumentException("Seed must be at least as long as bit length");
    }
    this.next = truncate(seed, modulus, modBitLength);
    this.rand = new Random(42);
  }

  private FieldElement truncate(StrictBitVector seed, BigInteger modulus, int modBitLength) {
    BigInteger val = new BigInteger(1, seed.toByteArray()).mod(modulus);
    return new FieldElement(val, modulus, modBitLength);
  }

  @Override
  public FieldElement getNext(BigInteger modulus, int bitLength) {
    BigInteger randomOffset = new BigInteger(bitLength, rand).mod(modulus);
    next = next.add(new FieldElement(randomOffset, modulus, bitLength));
    return next;
  }

}
