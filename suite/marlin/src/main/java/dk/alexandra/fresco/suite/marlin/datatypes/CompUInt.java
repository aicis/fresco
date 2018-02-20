package dk.alexandra.fresco.suite.marlin.datatypes;

public interface CompUInt<HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    T extends UInt<T>> extends UInt<T> {

  HighT getHigh();

  LowT getLow();

  LowT computeOverflow();

}
