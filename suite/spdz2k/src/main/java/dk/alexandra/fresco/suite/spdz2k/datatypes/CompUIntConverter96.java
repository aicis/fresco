package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class CompUIntConverter96 implements CompUIntConverter<UInt64, UInt32, CompUInt96> {

  @Override
  public CompUInt96 createFromHigh(UInt64 value) {
    return new CompUInt96(value);
  }

  @Override
  public CompUInt96 createFromLow(UInt32 value) {
    return new CompUInt96(value);
  }

}
