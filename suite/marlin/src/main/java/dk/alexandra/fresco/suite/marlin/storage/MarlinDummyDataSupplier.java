package dk.alexandra.fresco.suite.marlin.storage;

import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import java.math.BigInteger;

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
        BigInteger.ONE.shiftLeft(factory.getBitLength()));
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
    return null;
  }

  @Override
  public T getSecretSharedKey() {
    return secretSharedKey;
  }

  @Override
  public MarlinSInt<T> getNextRandomElement() {
    return new MarlinSInt<>(toMarlinElement(supplier.getRandomElementShare()));
  }

  private MarlinElement<T> toMarlinElement(Pair<BigInteger, BigInteger> raw) {
    T openValue = factory.createFromBigInteger(raw.getFirst());
    T share = factory.createFromBigInteger(raw.getSecond());
    T macShare = openValue.multiply(secretSharedKey);
    return new MarlinElement<>(share, macShare);
  }

}
