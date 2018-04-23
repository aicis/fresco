package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import java.math.BigInteger;

public class CompUIntOIntFactory<CompT extends CompUInt<?, ?, CompT>> implements OIntFactory {

  private final CompUIntFactory<CompT> factory;

  public CompUIntOIntFactory(
      CompUIntFactory<CompT> factory) {
    this.factory = factory;
  }

  @Override
  public BigInteger toBigInteger(OInt value) {
    return ((CompUInt) value).toBigInteger();
  }

  @Override
  public OInt fromBigInteger(BigInteger value) {
    return factory.createFromBigInteger(value);
  }

  @Override
  public OInt zero() {
    return null;
  }

  @Override
  public OInt one() {
    return null;
  }

  @Override
  public OInt two() {
    return null;
  }
}
