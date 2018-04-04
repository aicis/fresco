package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class CompUIntConverterGeneric implements
    CompUIntConverter<GenericCompUInt, GenericCompUInt, GenericCompUInt> {

  private final int highBitLength;
  private final int lowBitLength;

  public CompUIntConverterGeneric(int highBitLength, int lowBitLength) {
    this.highBitLength = highBitLength;
    this.lowBitLength = lowBitLength;
  }

  @Override
  public GenericCompUInt createFromHigh(GenericCompUInt value) {
    return new GenericCompUInt(value, getCompositeBitLength());
  }

  @Override
  public GenericCompUInt createFromLow(GenericCompUInt value) {
    return new GenericCompUInt(value, getCompositeBitLength());
  }

  private int getCompositeBitLength() {
    return highBitLength + lowBitLength;
  }

}
