package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Random;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

// this class is temporary and will go away once we have a real implementation
public class DummyPrg implements FieldElementPrg {

  private Random rand;

  public DummyPrg(StrictBitVector seed) throws IllegalArgumentException {
    if (seed.getSize() > Long.BYTES * 8) {
      throw new IllegalArgumentException("Dummy prg only works for seeds up to size of long");
    }
    this.rand = new Random(truncate(seed));
  }

  private long truncate(StrictBitVector seed) {
    byte[] ba = seed.toByteArray();
    byte[] padded = new byte[Long.BYTES];
    System.arraycopy(ba, 0, padded, 0, ba.length);
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.put(padded);
    buffer.flip(); // need flip
    return buffer.getLong();
  }

  @Override
  public FieldElement getNext(BigInteger modulus, int bitLength) {
    FieldElement next = null;
    while (next == null) {
      try {
        byte[] arr = new byte[bitLength / 8];
        rand.nextBytes(arr);
        next = new FieldElement(arr, modulus, bitLength);
      } catch (IllegalArgumentException e) {
        next = null;
      }
    }
    return next;
  }

}
