package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class CompUInt64Bit extends CompUInt64 {

  // TODO switch to smarter representation

  public CompUInt64Bit(int high, int bit) {
    super((UInt.toUnLong(high) << 32) | UInt.toUnLong(bit) << 31);
  }

  public CompUInt64Bit(long value) {
    super(value);
  }

  @Override
  public CompUInt64 toBitRep() {
    throw new IllegalStateException("Already in bit form.");
  }

  @Override
  public CompUInt64 toArithmeticRep() {
    return new CompUInt64(value);
  }

  @Override
  public CompUInt64 multiply(CompUInt64 other) {
    return new CompUInt64Bit(((value >>> 31) * (other.value >>> 31)) << 31);
  }

  @Override
  public CompUInt64 add(CompUInt64 other) {
    return new CompUInt64Bit(((value >>> 31) + (other.value >>> 31)) << 31);
  }

  @Override
  public String toString() {
    return toBigInteger().toString() + "B";
  }

  @Override
  public CompUInt64 subtract(CompUInt64 other) {
    throw new UnsupportedOperationException("Subtraction not supported by bit representation");
  }

  @Override
  public CompUInt64 negate() {
    throw new UnsupportedOperationException("Negation not supported by bit representation");
  }

  @Override
  public int bitValue() {
    return (int) (((value & 0x80000000L)) >>> 31);
  }

  @Override
  public byte[] serializeLeastSignificant() {
    return new byte[]{(byte) bitValue()};
  }

  @Override
  public CompUInt64 clearHighBits() {
    return new CompUInt64Bit(value & 0xffffffffL);
  }

  @Override
  public CompUInt64 multiplyByBit(int bitValue) {
    return this.multiply(new CompUInt64Bit(0, bitValue));
  }

}
