package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.framework.util.MultiplicationTripleShares;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kTriple;
import java.math.BigInteger;

public class Spdz2kDummyDataSupplier<
    T extends CompUInt<?, ?, T>> implements
    Spdz2kDataSupplier<T> {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final T secretSharedKey;
  private final CompUIntFactory<T> factory;

  public Spdz2kDummyDataSupplier(int myId, int noOfParties, T secretSharedKey,
      CompUIntFactory<T> factory) {
    this.myId = myId;
    this.secretSharedKey = secretSharedKey;
    this.factory = factory;
    this.supplier = new ArithmeticDummyDataSupplier(myId, noOfParties,
        BigInteger.ONE.shiftLeft(factory.getCompositeBitLength()));
  }

  @Override
  public Spdz2kTriple<T> getNextTripleShares() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new Spdz2kTriple<>(
        toMarlinElement(rawTriple.getLeft()),
        toMarlinElement(rawTriple.getRight()),
        toMarlinElement(rawTriple.getProduct()));
  }

  @Override
  public Spdz2kInputMask<T> getNextInputMask(int towardPlayerId) {
    Pair<BigInteger, BigInteger> raw = supplier.getRandomElementShare();
    if (myId == towardPlayerId) {
      return new Spdz2kInputMask<>(toMarlinElement(raw),
          factory.createFromBigInteger(raw.getFirst()));
    } else {
      return new Spdz2kInputMask<>(toMarlinElement(raw));
    }
  }

  @Override
  public Spdz2kSInt<T> getNextBitShare() {
    return toMarlinElement(supplier.getRandomBitShare());
  }

  @Override
  public T getSecretSharedKey() {
    return secretSharedKey;
  }

  @Override
  public Spdz2kSInt<T> getNextRandomElementShare() {
    return toMarlinElement(supplier.getRandomElementShare());
  }

  private Spdz2kSInt<T> toMarlinElement(Pair<BigInteger, BigInteger> raw) {
    T openValue = factory.createFromBigInteger(raw.getFirst());
    T share = factory.createFromBigInteger(raw.getSecond());
    T macShare = openValue.multiply(secretSharedKey);
    return new Spdz2kSInt<>(share, macShare);
  }

}
