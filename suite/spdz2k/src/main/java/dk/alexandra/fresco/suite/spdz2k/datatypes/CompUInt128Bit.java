package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class CompUInt128Bit extends CompUInt128 {

  CompUInt128Bit(long high, int mid, int low) {
    super(high, mid, low);
  }

  CompUInt128Bit(long high, int bit) {
    super(high, bit << 31, 0);
  }

  CompUInt128Bit(CompUInt128 other) {
    super(other);
  }

  @Override
  public CompUInt128 multiply(CompUInt128 other) {
    int bit = mid >>> 31;
    int otherBit = other.mid >>> 31;
    return new CompUInt128Bit(
        ((high * other.high) << 1) + (high * otherBit) + (other.high * bit),
        mid & other.mid,
        0);
  }

  @Override
  public CompUInt128 add(CompUInt128 other) {
    return new CompUInt128Bit(high + other.high, mid ^ other.mid, low ^ other.low);
  }

  @Override
  public CompUInt128 subtract(CompUInt128 other) {
    return new CompUInt128Bit(high - other.high, mid ^ (~other.mid), low ^ (~other.low));
  }

  @Override
  public CompUInt128 toBitRep() {
    throw new IllegalStateException("Already in bit form");
  }

  public boolean getValueBit() {
    return testBit(63);
  }

}
