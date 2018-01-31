package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.util.SameTypePair;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

public class SpdzDummyDataSupplier implements SpdzDataSupplier {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final BigInteger modulus;
  private final BigInteger secretSharedKey;
  private final int expPipeLength;

  public SpdzDummyDataSupplier(int myId, int noOfPlayers) {
    // TODO kill this
    this(myId, noOfPlayers, getDefaultModulus(noOfPlayers), getSsk(getDefaultModulus(noOfPlayers)));
  }

  public SpdzDummyDataSupplier(int myId, int noOfPlayers, BigInteger modulus,
      BigInteger secretSharedKey) {
    this(myId, noOfPlayers, modulus, secretSharedKey, 200);
  }

  public SpdzDummyDataSupplier(int myId, int noOfPlayers, BigInteger modulus,
      BigInteger secretSharedKey, int expPipeLength) {
    this.myId = myId;
    this.modulus = modulus;
    this.secretSharedKey = secretSharedKey;
    this.expPipeLength = expPipeLength;
    this.supplier = new ArithmeticDummyDataSupplier(myId, noOfPlayers, modulus);
  }

  @Override
  public SpdzTriple getNextTriple() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new SpdzTriple(
        toSpdzElement(rawTriple.getLeft()),
        toSpdzElement(rawTriple.getRight()),
        toSpdzElement(rawTriple.getProduct()));
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    List<SameTypePair<BigInteger>> rawExpPipe = supplier.getExpPipe(expPipeLength);
    return rawExpPipe.stream()
        .map(r -> new SpdzSInt(toSpdzElement(r)))
        .toArray(SpdzSInt[]::new);
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

  // TODO kill this
  static private BigInteger getDefaultModulus(int noOfPlayers) {
    if (noOfPlayers == 2) {
      return new BigInteger(
          "2582249878086908589655919172003011874329705792829223512830659356540647622016841194629645353280137831435903171972747493557");
    } else {
      return new BigInteger(
          "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
    }
  }

  // TODO kill this
  static private BigInteger getSsk(BigInteger modulus) {
    return new BigInteger(modulus.bitLength(), new Random()).mod(modulus);
  }

}
