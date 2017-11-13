package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import java.math.BigInteger;

/**
 * <p> This protocol implements integer division where both numerator and denominator are secret
 * shared. If the denominator is a known number {@link KnownDivisor} should be used instead. </p>
 *
 * <p> The protocol uses <a href= "https://en.wikipedia.org/wiki/Division_algorithm#Goldschmidt_division"
 * >Goldschmidt Division</a> (aka. the 'IBM Method'). </p>
 *
 * Its results approximate regular integer division with n bits, where n is equal to {@link
 * BasicNumericContext#getMaxBitLength()} / 4. Just like
 * regular integer division, this division will always truncate the result instead of rounding.
 */
public class SecretSharedDivisor
    implements Computation<SInt, ProtocolBuilderNumeric> {

  private DRes<SInt> numerator;
  private DRes<SInt> denominator;

  public SecretSharedDivisor(
      DRes<SInt> numerator,
      DRes<SInt> denominator) {
    this.numerator = numerator;
    this.denominator = denominator;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {

    BasicNumericContext basicNumericContext = builder.getBasicNumericContext();

    // Calculate maximum number of bits we can represent without overflows.
    // We lose half of the precision because we need to multiply two numbers without overflow.
    // And we lose half again because we need to be able to shift the numerator left,
    // depending on the bit length of the denominator
    int maximumBitLength = basicNumericContext.getMaxBitLength() / 4;

    // Calculate amount of iterations that are needed to get a precise answer in all decimal bits
    int amountOfIterations = log2(maximumBitLength);

    // Convert 2 to fixed point notation with 'maximumBitLength' decimals.
    BigInteger two = BigInteger.valueOf(2).shiftLeft(maximumBitLength);

    return builder.seq(seq -> Pair.lazy(numerator, denominator)
    ).pairInPar((seq, pair) -> {
      // Determine sign of numerator and ensure positive
      DRes<SInt> numerator = pair.getFirst();
      DRes<SInt> sign = seq.comparison().sign(numerator);

      return Pair.lazy(sign, seq.numeric().mult(sign, numerator));
    }, (seq, pair) -> {
      // Determine sign of denominator and ensure positive
      DRes<SInt> denominator = pair.getSecond();
      DRes<SInt> sign = seq.comparison().sign(denominator);

      return Pair.lazy(sign, seq.numeric().mult(sign, denominator));
    }).seq((seq, pair) -> {
      DRes<SInt> denominator = pair.getSecond().getSecond();
      // Determine the actual number of bits in the denominator.
      DRes<SInt> denominatorBitLength = getBitLength(seq, denominator, maximumBitLength);
      // Determine the maximum number of bits we can shift the denominator left in order to gain more precision.
      BigInteger maxBitLength = BigInteger.valueOf(maximumBitLength);
      DRes<SInt> leftShift = seq.numeric().sub(maxBitLength, denominatorBitLength);
      DRes<SInt> leftShiftFactor = exp2(seq, leftShift, log2(maximumBitLength));
      return Pair.lazy(leftShiftFactor, pair);
      // Left shift numerator and denominator for greater precision.
      // We're allowed to do this because shifting numerator and denominator by the same amount
      // doesn't change the outcome of the division.
    }).pairInPar((seq, pair) -> {
          DRes<SInt> numeratorSign = pair.getSecond().getFirst().getFirst();
          DRes<SInt> numerator = pair.getSecond().getFirst().getSecond();
          DRes<SInt> shiftNumerator = seq.numeric().mult(pair.getFirst(), numerator);
          return Pair.lazy(numeratorSign, shiftNumerator);
        },
        (seq, pair) -> {
          DRes<SInt> denomintator = pair.getSecond().getSecond().getSecond();
          DRes<SInt> denomintatorSign = pair.getSecond().getSecond().getFirst();
          DRes<SInt> shiftedDenominator = seq.numeric().mult(pair.getFirst(), denomintator);
          return Pair.lazy(denomintatorSign, shiftedDenominator);
        }
    ).seq((seq, pair) -> {
      DRes<Pair<SInt, SInt>> iterationPair = Pair
          .lazy(pair.getFirst().getSecond().out(), pair.getSecond().getSecond().out());
      // Goldschmidt iteration
      for (int i = 0; i < amountOfIterations; i++) {
        DRes<Pair<SInt, SInt>> finalPair = iterationPair;
        iterationPair = seq.seq((innerSeq) -> {
          Pair<SInt, SInt> iteration = finalPair.out();
          DRes<SInt> d = iteration::getSecond;
          DRes<SInt> f = innerSeq.numeric().sub(two, d);
          return Pair.lazy(f, iteration);
        }).pairInPar((innerSeq, innerPair) -> {
          Numeric innerNumeric = innerSeq.numeric();
          DRes<SInt> n = () -> innerPair.getSecond().getFirst();
          DRes<SInt> f = innerPair.getFirst();
          return shiftRight(innerSeq, innerNumeric.mult(f, n), maximumBitLength);
        }, (innerSeq, innerPair) -> {
          Numeric innerNumeric = innerSeq.numeric();
          DRes<SInt> d = () -> innerPair.getSecond().getSecond();
          DRes<SInt> f = innerPair.getFirst();
          return shiftRight(innerSeq, innerNumeric.mult(f, d), maximumBitLength);
        });
      }
      DRes<Pair<SInt, SInt>> iterationResult = iterationPair;
      return () -> new Pair<>(
          iterationResult.out().getFirst(),
          new Pair<>(pair.getFirst().getFirst(), pair.getSecond().getFirst()));
    }).seq((seq, pair) -> {
      DRes<SInt> n = pair::getFirst;
      Pair<DRes<SInt>, DRes<SInt>> signs = pair.getSecond();
      Numeric numeric = seq.numeric();
      // Right shift to remove decimals, rounding last decimal up.
      n = numeric.add(BigInteger.ONE, n);
      n = shiftRight(seq, n, maximumBitLength);
      // Ensure that result has the correct sign
      n = numeric.mult(n, signs.getFirst());
      n = numeric.mult(n, signs.getSecond());
      return n;
    });
  }

  private int log2(int number) {
    return (int) Math.ceil(Math.log(number) / Math.log(2));
  }

  private DRes<SInt> getBitLength(ProtocolBuilderNumeric builder, DRes<SInt> input,
      int maximumBitLength) {
    return builder.advancedNumeric()
        .bitLength(input, maximumBitLength);
  }

  private DRes<SInt> exp2(ProtocolBuilderNumeric builder, DRes<SInt> exponent,
      int maxExponentLength) {
    return builder.advancedNumeric().exp(
        BigInteger.valueOf(2),
        exponent,
        maxExponentLength
    );
  }

  private DRes<SInt> shiftRight(ProtocolBuilderNumeric builder, DRes<SInt> input,
      int numberOfPositions) {
    return builder.advancedNumeric()
        .rightShift(input, numberOfPositions);
  }
}
