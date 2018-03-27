package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class CompUInt128Accumulator implements Accumulator<CompUInt128> {

  private long high;
  private int mid;
  private int low;

  public CompUInt128Accumulator(CompUInt128 value) {
    this.high = value.getHigh();
    this.mid = value.getMid();
    this.low = value.getLow();
  }

  @Override
  public CompUInt128 getResult() {
    return new CompUInt128(high, mid, low);
  }

  @Override
  public void add(CompUInt128 value) {
    long newLow = Integer.toUnsignedLong(this.low) + Integer.toUnsignedLong(value.getLow());
    long lowOverflow = newLow >>> 32;
    long newMid = Integer.toUnsignedLong(this.mid)
        + Integer.toUnsignedLong(value.getMid())
        + lowOverflow;
    long midOverflow = newMid >>> 32;
    this.high += value.getHigh() + midOverflow;
    this.mid = (int) newMid;
    this.low = (int) newLow;
  }

}
