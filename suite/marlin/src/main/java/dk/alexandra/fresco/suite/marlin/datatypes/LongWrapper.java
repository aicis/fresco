package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import java.math.BigInteger;

public class LongWrapper extends CompositeUInt128 {

  private final long value;

  public LongWrapper(long value) {
    this.value = value;
  }

  @Override
  public LongWrapper add(CompositeUInt128 other) {
    return new LongWrapper(value + other.getLowAsLong());
  }

  @Override
  public LongWrapper multiply(CompositeUInt128 other) {
    return new LongWrapper(value * other.getLowAsLong());
  }

  @Override
  public LongWrapper subtract(CompositeUInt128 other) {
    return new LongWrapper(value - other.getLowAsLong());
  }

  @Override
  public LongWrapper negate() {
    return new LongWrapper(-value);
  }

  @Override
  public boolean isZero() {
    return value == 0;
  }

  @Override
  public int getBitLength() {
    return 64;
  }

  @Override
  public byte[] toByteArray() {
    return ByteAndBitConverter.toByteArray(value);
  }

  @Override
  public BigInteger toBigInteger() {
    // TODO optimize if bottle-neck
    return new BigInteger(1, toByteArray());
  }

  @Override
  public LongWrapper computeOverflow() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongWrapper getSubRange(int from, int to) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongWrapper getLow() {
    return new LongWrapper(value);
  }

  @Override
  public LongWrapper getHigh() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongWrapper shiftLowIntoHigh() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getLowAsLong() {
    return value;
  }

}
