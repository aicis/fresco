package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.util.SameTypePair;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;

public class SpdzConfigurableDataSupplier implements SpdzDataSupplier {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final BigInteger modulus;
  private final BigInteger secretSharedKey;

  public SpdzConfigurableDataSupplier(int myId, int noOfPlayers, BigInteger modulus,
      BigInteger secretSharedKey) {
    this.myId = myId;
    this.modulus = modulus;
    this.secretSharedKey = secretSharedKey;
    this.supplier = new ArithmeticDummyDataSupplier(myId, noOfPlayers, modulus);
  }

  @Override
  public SpdzTriple getNextTriple() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new SpdzTriple(
        toSpdzElement(rawTriple.getLeft()),
        toSpdzElement(rawTriple.getLeft()),
        toSpdzElement(rawTriple.getRight()));
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    // TODO
    return new SpdzSInt[0];
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerId) {
    SameTypePair<BigInteger> raw = supplier.getRandomElementShare();
    if (myId == towardPlayerId) {
      return new SpdzInputMask(toSpdzElement(raw), raw.getFirst());
    } else {
      return new SpdzInputMask(toSpdzElement(raw), null);
    }
  }

  @Override
  public SpdzSInt getNextBit() {
    return new SpdzSInt(toSpdzElement(supplier.getRandomBitShare()));
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public BigInteger getSecretSharedKey() {
    return secretSharedKey;
  }

  @Override
  public SpdzSInt getNextRandomFieldElement() {
    return new SpdzSInt(toSpdzElement(supplier.getRandomElementShare()));
  }

  private SpdzElement toSpdzElement(SameTypePair<BigInteger> raw) {
    return new SpdzElement(
        raw.getSecond(),
        raw.getFirst().multiply(secretSharedKey).mod(modulus),
        modulus
    );
  }
}
