package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.TruncationPair;
import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.framework.util.MultiplicationTripleShares;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.TruncationPairShares;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

public class SpdzDummyDataSupplier implements SpdzDataSupplier {

  private static int DEFAULT_MAX_BIT_LENGTH = 512;

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final BigInteger modulus;
  private final BigInteger wholeKey;
  private final BigInteger myKeyShare;
  private final int expPipeLength;
  private final int maxBitLength;

  public SpdzDummyDataSupplier(int myId, int noOfPlayers) {
    // TODO kill this
    this(myId, noOfPlayers, ModulusFinder.findSuitableModulus(512),
        getSsk(ModulusFinder.findSuitableModulus(512)));
  }

  public SpdzDummyDataSupplier(int myId, int noOfPlayers, BigInteger modulus) {
    // TODO kill this
    this(myId, noOfPlayers, modulus, getSsk(modulus));
  }

  public SpdzDummyDataSupplier(int myId, int noOfPlayers, BigInteger modulus,
      BigInteger secretSharedKey) {
    this(myId, noOfPlayers, modulus, secretSharedKey, 200);
  }

  public SpdzDummyDataSupplier(int myId, int noOfPlayers, BigInteger modulus,
      BigInteger secretSharedKey, int expPipeLength) {
    this(myId, noOfPlayers, modulus, secretSharedKey, expPipeLength, modulus.bitLength() - 1);
  }

  public SpdzDummyDataSupplier(int myId, int noOfPlayers, BigInteger modulus,
      BigInteger secretSharedKey, int expPipeLength, int maxBitLength) {
    this.myId = myId;
    this.modulus = modulus;
    this.expPipeLength = expPipeLength;
    this.maxBitLength = maxBitLength;
    this.supplier = new ArithmeticDummyDataSupplier(myId, noOfPlayers, modulus,
        BigInteger.ONE.shiftLeft(maxBitLength - 1));
    final Pair<BigInteger, BigInteger> keyPair = supplier.getRandomElementShare();
    this.wholeKey = keyPair.getFirst();
    this.myKeyShare = keyPair.getSecond();
  }

  @Override
  public SpdzTriple getNextTriple() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new SpdzTriple(
        toSpdzSInt(rawTriple.getLeft()),
        toSpdzSInt(rawTriple.getRight()),
        toSpdzSInt(rawTriple.getProduct()));
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    List<Pair<BigInteger, BigInteger>> rawExpPipe = supplier.getExpPipe(expPipeLength);
    return rawExpPipe.stream()
        .map(this::toSpdzSInt)
        .toArray(SpdzSInt[]::new);
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerId) {
    Pair<BigInteger, BigInteger> raw = supplier.getRandomElementShare();
    if (myId == towardPlayerId) {
      return new SpdzInputMask(toSpdzSInt(raw), raw.getFirst());
    } else {
      return new SpdzInputMask(toSpdzSInt(raw), null);
    }
  }

  @Override
  public SpdzSInt getNextBit() {
    return toSpdzSInt(supplier.getRandomBitShare());
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public BigInteger getSecretSharedKey() {
    return myKeyShare;
  }

  @Override
  public SpdzSInt getNextRandomFieldElement() {
    return toSpdzSInt(supplier.getRandomElementShare());
  }

  @Override
  public TruncationPair getNextTruncationPair(int d) {
    TruncationPairShares pair = supplier.getTruncationPairShares(d);
    return new TruncationPair(toSpdzSInt(pair.getRPrime()), toSpdzSInt(pair.getR()));
  }

  private SpdzSInt toSpdzSInt(Pair<BigInteger, BigInteger> raw) {
    return new SpdzSInt(
        raw.getSecond(),
        raw.getSecond().multiply(wholeKey).mod(modulus),
        modulus
    );
  }

  static private BigInteger getSsk(BigInteger modulus) {
    return new BigInteger(modulus.bitLength(), new Random()).mod(modulus);
  }

}
