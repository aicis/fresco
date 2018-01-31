package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.util.SameTypePair;
import dk.alexandra.fresco.framework.util.SecretSharer;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Supplies generic pre-processed material common across arithmetic SPDZ-like suites, including
 * random elements, bit, and multiplication triples. <p>Uses {@link Random} to deterministically
 * generate all material. NOT secure.</p>
 */
public class ArithmeticDummyDataSupplier {

  private final int myId;
  private final int noOfParties;
  private final BigInteger modulus;
  private final int modBitLength;
  private final Random random;
  private final SecretSharer<BigInteger> sharer;

  public ArithmeticDummyDataSupplier(int myId, int noOfParties, BigInteger modulus) {
    this.myId = myId;
    this.noOfParties = noOfParties;
    this.modulus = modulus;
    this.modBitLength = modulus.bitLength();
    random = new Random(42);
    sharer = new DummyBigIntegerSharer(modulus, random);
  }

  /**
   * Computes the next random element and this party's share.
   */
  public SameTypePair<BigInteger> getRandomElementShare() {
    BigInteger element = getNextRandomElement();
    return new SameTypePair<>(element, sharer.share(element, noOfParties).get(myId - 1));
  }

  /**
   * Computes the next random bit (expressed as {@link BigInteger}) and this party's share.
   */
  public SameTypePair<BigInteger> getRandomBitShare() {
    BigInteger bit = getNextBit();
    return new SameTypePair<>(bit, sharer.share(bit, noOfParties).get(myId - 1));
  }

  /**
   * Computes the next random multiplication triple and this party's shares.
   */
  public MultiplicationTripleShares getMultiplicationTripleShares() {
    BigInteger left = getNextRandomElement();
    BigInteger right = getNextRandomElement();
    BigInteger product = left.multiply(right).mod(modulus);
    return new MultiplicationTripleShares(
        new SameTypePair<>(left, sharer.share(left, noOfParties).get(myId - 1)),
        new SameTypePair<>(right, sharer.share(right, noOfParties).get(myId - 1)),
        new SameTypePair<>(product, sharer.share(product, noOfParties).get(myId - 1))
    );
  }

  private BigInteger getNextRandomElement() {
    return new BigInteger(modBitLength, random).mod(modulus);
  }

  private BigInteger getNextBit() {
    return random.nextBoolean() ? BigInteger.ONE : BigInteger.ZERO;
  }

  class DummyBigIntegerSharer implements SecretSharer<BigInteger> {

    private final BigInteger modulus;
    private final int modBitLength;
    private final Random random;

    DummyBigIntegerSharer(BigInteger modulus, Random random) {
      this.modulus = modulus;
      this.modBitLength = modulus.bitLength();
      this.random = random;
    }

    /**
     * Computes an additive secret-sharing of the input element.
     */
    @Override
    public List<BigInteger> share(BigInteger input, int numShares) {
      List<BigInteger> shares = getNextRandomElements(numShares - 1);
      BigInteger sumShares = MathUtils.sum(shares, modulus);
      BigInteger diff = input.subtract(sumShares).mod(modulus);
      shares.add(diff);
      return shares;
    }

    /**
     * Recombines additive secret-shares into secret.
     */
    public BigInteger recombine(List<BigInteger> shares) {
      return MathUtils.sum(shares, modulus);
    }

    private List<BigInteger> getNextRandomElements(int numElements) {
      return IntStream.range(0, numElements)
          .mapToObj(i -> new BigInteger(modBitLength, random).mod(modulus))
          .collect(Collectors.toList());
    }

  }

}
