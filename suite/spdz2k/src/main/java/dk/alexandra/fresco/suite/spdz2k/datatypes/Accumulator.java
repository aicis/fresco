package dk.alexandra.fresco.suite.spdz2k.datatypes;

public interface Accumulator<T extends UInt<T>>  {

  T getResult();

  void add(T value);

}
