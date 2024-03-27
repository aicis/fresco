package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Supplies generic pre-processed material common across arithmetic SPDZ-like suites, including
 * random elements, bits, and multiplication triples.
 *
 * <p>Uses {@link Random} to deterministically generate all material. This is not secure, and should not be used in production code!
 */
public class ArithmeticDummyDataSupplier {

  private final int myId;
  private final int noOfParties;
  private final BigInteger modulus;
  private final int modBitLength;
  private final Random random;
  private final SecretSharer<BigInteger> sharer;
  private final ModularReductionAlgorithm reducer;

  public ArithmeticDummyDataSupplier(int myId, int noOfParties, BigInteger modulus) {
    ValidationUtils.assertValidId(myId, noOfParties);
    this.myId = myId;
    this.noOfParties = noOfParties;
    this.modulus = modulus;
    this.modBitLength = modulus.bitLength();
    random = new Random(42);
    sharer = new DummyBigIntegerSharer(modulus, random);
    reducer = ModularReductionAlgorithm.getReductionAlgorithm(modulus);
  }

  /**
   * Computes the next random element and this party's share.
   */
  public Pair<BigInteger, BigInteger> getRandomElementShare() {
    BigInteger element = sampleRandomBigInteger();
    return new Pair<>(element, sharer.share(element, noOfParties).get(myId - 1));
  }

  /**
   * Computes the next random bit (expressed as {@link BigInteger}) and this party's share.
   */
  public Pair<BigInteger, BigInteger> getRandomBitShare() {
    BigInteger bit = getNextBit();
    return new Pair<>(bit, sharer.share(bit, noOfParties).get(myId - 1));
  }

  /**
   * Computes the next random multiplication triple and this party's shares.
   */
  public MultiplicationTripleShares getMultiplicationTripleShares() {
    BigInteger left = sampleRandomBigInteger();
    BigInteger right = sampleRandomBigInteger();
    BigInteger product = reducer.apply(left.multiply(right));
    return new MultiplicationTripleShares(
        new Pair<>(left, sharer.share(left, noOfParties).get(myId - 1)),
        new Pair<>(right, sharer.share(right, noOfParties).get(myId - 1)),
        new Pair<>(product, sharer.share(product, noOfParties).get(myId - 1))
    );
  }

  /**
   * Constructs an exponentiation pipe. <p>An exponentiation pipe is a list of numbers in the
   * following format: r^{-1}, r, r^{2}, r^{3}, ..., r^{expPipeLength}, where r is a random element
   * and all exponentiations are mod {@link #modulus}.</p>
   */
  public List<Pair<BigInteger, BigInteger>> getExpPipe(int expPipeLength) {
    List<BigInteger> openExpPipe = getOpenExpPipe(expPipeLength);
    return openExpPipe.stream()
        .map(r -> new Pair<>(r, sharer.share(r, noOfParties).get(myId - 1)))
        .collect(Collectors.toList());
  }

  private BigInteger sampleRandomBigInteger() {
    return reducer.apply(new BigInteger(modBitLength, random));
  }

  private List<BigInteger> getOpenExpPipe(int expPipeLength) {
    List<BigInteger> openExpPipe = new ArrayList<>(expPipeLength);
    BigInteger first = sampleRandomBigInteger();
    BigInteger inverse = first.modInverse(modulus);
    openExpPipe.add(inverse);
    openExpPipe.add(first);
    for (int i = 1; i < expPipeLength; i++) {
      BigInteger previous = openExpPipe.get(openExpPipe.size() - 1);
      openExpPipe.add(reducer.apply(previous.multiply(first)));
    }
    return openExpPipe;
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
      BigInteger diff = reducer.apply(input.subtract(sumShares));
      shares.add(diff);
      return shares;
    }

    /**
     * Recombines additive secret-shares into secret.
     */
    @Override
    public BigInteger recombine(List<BigInteger> shares) {
      return MathUtils.sum(shares, modulus);
    }

    private List<BigInteger> getNextRandomElements(int numElements) {
      return IntStream.range(0, numElements)
          .mapToObj(i -> reducer.apply(new BigInteger(modBitLength, random)))
          .collect(Collectors.toList());
    }
  }

}
