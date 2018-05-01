package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.math.BigInteger;

public class CompUInt128Bit extends CompUInt128 {

  public CompUInt128Bit(byte[] bytes) {
    super(bytes);
  }

  public CompUInt128Bit(byte[] bytes, boolean requiresPadding) {
    super(bytes, requiresPadding);
  }

  public CompUInt128Bit(BigInteger value) {
    super(value);
  }

  CompUInt128Bit(long high, int mid, int low) {
    super(high, mid, low);
  }

  CompUInt128Bit(UInt64 value) {
    super(value);
  }

  CompUInt128Bit(long value) {
    super(value);
  }

  CompUInt128Bit(CompUInt128 other) {
    super(other);
  }

  @Override
  public CompUInt128 add(CompUInt128 other) {
    return new CompUInt128Bit(high + other.high, mid & other.mid, low & other.low);
  }

  @Override
  public CompUInt128 multiply(CompUInt128 other) {
    return new CompUInt128Bit(high + other.high, mid & other.mid, low & other.low);
  }

  @Override
  public CompUInt128 toBitRepresentation() {
    throw new IllegalStateException("Already in bit form");
  }

}
