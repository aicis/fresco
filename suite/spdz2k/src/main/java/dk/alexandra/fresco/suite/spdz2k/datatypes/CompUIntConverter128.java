package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class CompUIntConverter128 implements CompUIntConverter<UInt64, UInt64, CompUInt128> {

  @Override
  public CompUInt128 createFromHigh(UInt64 value) {
    return new CompUInt128(value);
  }

  @Override
  public CompUInt128 createFromLow(UInt64 value) {
    return new CompUInt128(value);
  }

}
