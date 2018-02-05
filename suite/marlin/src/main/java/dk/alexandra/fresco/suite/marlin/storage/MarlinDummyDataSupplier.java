package dk.alexandra.fresco.suite.marlin.storage;

import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;

public class MarlinDummyDataSupplier<T extends BigUInt<T>> implements MarlinDataSupplier<T> {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final T secretSharedKey;
  private final BigUIntFactory<T> factory;

  public MarlinDummyDataSupplier(int myId, int noOfParties, T secretSharedKey,
      BigUIntFactory<T> factory) {
    this.myId = myId;
    this.secretSharedKey = secretSharedKey;
    this.factory = factory;
    this.supplier = new ArithmeticDummyDataSupplier(myId, noOfParties,
        factory.createRandom().toBigInteger());
  }

  @Override
  public MarlinTriple getNextTriple() {
    return null;
  }

  @Override
  public MarlinInputMask getNextInputMask(int towardPlayerId) {
    return null;
  }

  @Override
  public MarlinSInt<T> getNextBit() {
    return new MarlinSInt<>(
        new MarlinElement<>(
            factory.createFromBytes(new byte[]{}),
            factory.createFromBytes(new byte[]{}))
    );
  }

  @Override
  public T getSecretSharedKey() {
    return null;
  }

  @Override
  public MarlinSInt<T> getNextRandomFieldElement() {
    return null;
  }

}
