package dk.alexandra.fresco.suite.marlin.resource.storage;

import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.framework.util.MultiplicationTripleShares;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import java.math.BigInteger;

public class MarlinDummyDataSupplier<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> implements
    MarlinDataSupplier<H, L, T> {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final T secretSharedKey;
  private final CompUIntFactory<H, L, T> factory;

  public MarlinDummyDataSupplier(int myId, int noOfParties, T secretSharedKey,
      CompUIntFactory<H, L, T> factory) {
    this.myId = myId;
    this.secretSharedKey = secretSharedKey;
    this.factory = factory;
    this.supplier = new ArithmeticDummyDataSupplier(myId, noOfParties,
        BigInteger.ONE.shiftLeft(factory.getCompositeBitLength()));
  }

  @Override
  public MarlinTriple<H, L, T> getNextTripleShares() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new MarlinTriple<>(
        toMarlinElement(rawTriple.getLeft()),
        toMarlinElement(rawTriple.getRight()),
        toMarlinElement(rawTriple.getProduct()));
  }

  @Override
  public MarlinInputMask<H, L, T> getNextInputMask(int towardPlayerId) {
    Pair<BigInteger, BigInteger> raw = supplier.getRandomElementShare();
    if (myId == towardPlayerId) {
      return new MarlinInputMask<>(toMarlinElement(raw),
          factory.createFromBigInteger(raw.getFirst()));
    } else {
      return new MarlinInputMask<>(toMarlinElement(raw));
    }
  }

  @Override
  public MarlinSInt<H, L, T> getNextBitShare() {
    return toMarlinElement(supplier.getRandomBitShare());
  }

  @Override
  public T getSecretSharedKey() {
    return secretSharedKey;
  }

  @Override
  public MarlinSInt<H, L, T> getNextRandomElementShare() {
    return toMarlinElement(supplier.getRandomElementShare());
  }

  private MarlinSInt<H, L, T> toMarlinElement(Pair<BigInteger, BigInteger> raw) {
    T openValue = factory.createFromBigInteger(raw.getFirst());
    T share = factory.createFromBigInteger(raw.getSecond());
    T macShare = openValue.multiply(secretSharedKey);
    return new MarlinSInt<>(share, macShare);
  }

}
