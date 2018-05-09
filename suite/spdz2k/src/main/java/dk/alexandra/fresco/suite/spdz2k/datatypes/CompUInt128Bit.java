package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class CompUInt128Bit extends CompUInt128 {

  public CompUInt128Bit(long high, int mid, int low) {
    super(high, mid, low);
  }

  public CompUInt128Bit(long high, int bit) {
    super(high, bit << 31, 0);
  }

  public CompUInt128Bit(CompUInt128 other) {
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
    int carry = ((mid >>> 31) + (other.mid >>> 31)) >> 1;
    return new CompUInt128Bit(high + other.high + carry, mid ^ other.mid, 0);
  }

  @Override
  public CompUInt128 subtract(CompUInt128 other) {
    throw new UnsupportedOperationException("Subtraction not supported by bit representation");
  }

  @Override
  public CompUInt128 negate() {
    throw new UnsupportedOperationException("Negation not supported by bit representation");
  }

  @Override
  public CompUInt128 toBitRep() {
    throw new IllegalStateException("Already in bit form");
  }

  @Override
  public CompUInt128 toArithmeticRep() {
    return new CompUInt128(high, mid, low);
  }

  @Override
  public String toString() {
    return toBigInteger().toString() + "B";
  }

  @Override
  public byte[] serializeLeastSignificant() {
    return new byte[]{(byte) (mid >>> 31)};
  }

  @Override
  public CompUInt128 clearHighBits() {
    return new CompUInt128Bit(0L, mid, 0);
  }

  @Override
  public int bitValue() {
    return (mid & 0x80000000) >>> 31;
  }

  @Override
  public CompUInt128 multiplyByBit(int value) {
    return new CompUInt128Bit(high * value, mid & (value << 31), 0);
  }

}
