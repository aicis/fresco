package dk.alexandra.fresco.suite.spdz2k.datatypes;

import java.util.ArrayList;
import java.util.List;

public class CompUInt128Bit extends CompUInt128 {

  private static final CompUInt128Bit ONE = new CompUInt128Bit(0, 0x80000000, 0);

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
    CompUInt128 sum = super.add(other);
    return new CompUInt128Bit(sum.high, sum.mid, sum.low);
  }

  @Override
  public CompUInt128 subtract(CompUInt128 other) {
//    CompUInt128Bit test = ((CompUInt128Bit) other);
    CompUInt128 negated = new CompUInt128Bit(~other.high, ~other.mid & 0x80000000, 0).add(ONE);
    return this.add(negated);
  }

  @Override
  public CompUInt128 negate() {
    return new CompUInt128Bit(~high, ~mid & 0x80000000, 0).add(ONE);
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
    // TODO
    return this.multiply(new CompUInt128Bit(0L, value));
  }

  public boolean getValueBit() {
    return testBit(63);
  }

  public static void main(String[] args) {
    System.out.println("Hello world");
    int numProds = 10000000;
    CompUInt128Factory factory = new CompUInt128Factory();
    List<CompUInt128> values = new ArrayList<>(numProds);
    List<CompUInt128> bits = new ArrayList<>(numProds);
    for (int i = 0; i < numProds; i++) {
      CompUInt128 random = factory.createRandom();
      values.add(random);
      bits.add(random.toBitRep());
    }

    List<CompUInt128> result = new ArrayList<>(numProds);
    long start = System.currentTimeMillis();
    for (int i = 0; i < numProds; i++) {
      result.add(values.get(i).add(values.get(i)));
    }
    System.out.println(
        "time " + (System.currentTimeMillis() - start) + " " + result.get(123));

    List<CompUInt128> resultBits = new ArrayList<>(numProds);
    long startBits = System.currentTimeMillis();
    for (int i = 0; i < numProds; i++) {
      resultBits.add(bits.get(i).add(bits.get(i)));
    }
    System.out.println(
        "time " + (System.currentTimeMillis() - startBits) + " " + resultBits.get(123));

  }

}
