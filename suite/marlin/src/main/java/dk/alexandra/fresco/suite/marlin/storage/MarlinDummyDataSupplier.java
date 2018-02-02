package dk.alexandra.fresco.suite.marlin.storage;

import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinElement;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinInputMask;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinTriple;
import dk.alexandra.fresco.suite.marlin.datatypes.MutableUInt128;
import java.math.BigInteger;

public class MarlinDummyDataSupplier implements MarlinDataSupplier {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final BigInteger secretSharedKey;

  public MarlinDummyDataSupplier(int myId, int noOfParties, BigInteger secretSharedKey) {
    this.myId = myId;
    this.secretSharedKey = secretSharedKey;
    supplier = new ArithmeticDummyDataSupplier(myId, noOfParties, null);
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
  public MarlinSInt getNextBit() {
    return new MarlinSInt<>(
        new MarlinElement<>(new MutableUInt128(new byte[]{}), new MutableUInt128(new byte[]{})));
  }

  @Override
  public MutableUInt128 getSecretSharedKey() {
    return null;
  }

  @Override
  public MarlinSInt getNextRandomFieldElement() {
    return null;
  }

}
