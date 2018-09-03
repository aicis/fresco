package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class CompUIntConverter64 implements CompUIntConverter<UInt32, UInt32, CompUInt64> {

  @Override
  public CompUInt64 createFromHigh(UInt32 value) {
    return new CompUInt64(value.toInt());
  }

  @Override
  public CompUInt64 createFromLow(UInt32 value) {
    return new CompUInt64(value.toInt());
  }

}
