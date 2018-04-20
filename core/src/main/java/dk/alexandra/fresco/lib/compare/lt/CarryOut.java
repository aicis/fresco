package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.ArithmeticAndKnownRight;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given values a and b represented as bits, computes if a + b overflows, i.e., if there is a
 * carry.
 */
public class CarryOut implements Computation<SInt, ProtocolBuilderNumeric> {

  private static final BigInteger TWO = BigInteger.valueOf(2);
  private final DRes<List<DRes<BigInteger>>> clearBitsDef;
  private final DRes<List<DRes<SInt>>> secretBitsDef;
  private final BigInteger carryIn;

  /**
   * Constructs new {@link CarryOut}.
   *
   * @param clearBits clear bits
   * @param secretBits secret bits
   * @param carryIn an additional carry-in bit which we add to the least-significant bits of the
   * inputs
   */
  public CarryOut(DRes<List<DRes<BigInteger>>> clearBits, DRes<List<DRes<SInt>>> secretBits,
      BigInteger carryIn) {
    this.secretBitsDef = secretBits;
    this.clearBitsDef = clearBits;
    this.carryIn = carryIn;
  }

  /**
   * Default call to {@link #CarryOut(DRes, DRes, BigInteger)} without a carry-in.
   */
  public CarryOut(DRes<List<DRes<BigInteger>>> clearBits, DRes<List<DRes<SInt>>> secretBits) {
    this(clearBits, secretBits, BigInteger.ZERO);
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> secretBits = secretBitsDef.out();
    List<DRes<BigInteger>> clearBits = clearBitsDef.out();
    if (secretBits.size() != clearBits.size()) {
      throw new IllegalArgumentException("Number of bits must be the same");
    }
    return builder.par(new ArithmeticAndKnownRight(secretBitsDef, clearBitsDef))
        .par((par, andedBits) -> {
          List<DRes<SIntPair>> pairs = new ArrayList<>(andedBits.size());
          for (int i = 0; i < secretBits.size(); i++) {
            DRes<SInt> leftBit = secretBits.get(i);
            BigInteger rightBit = clearBits.get(i).out();
            DRes<SInt> andedBit = andedBits.get(i);
            // logical xor of two bits can is leftBit + rightBit - 2 * leftBit * rightBit
            DRes<SInt> xoredBit = par.seq(seq -> {
              Numeric nb = seq.numeric();
              return nb.sub(
                  nb.add(rightBit, leftBit),
                  nb.mult(TWO, andedBit)
              );
            });
            pairs.add(() -> new SIntPair(xoredBit, andedBit));
          }
          return () -> pairs;
        }).seq((seq, pairs) -> {
          // need to account for carry-in bit
          int lastIdx = pairs.size() - 1;
          SIntPair lastPair = pairs.get(lastIdx).out();
          DRes<SInt> lastCarryPropagator = seq.numeric().add(
              lastPair.getSecond(),
              seq.numeric().mult(carryIn, lastPair.getFirst()));
          pairs.set(lastIdx, () -> new SIntPair(lastPair.getFirst(), lastCarryPropagator));
          Collections.reverse(pairs);
          return seq.seq(new PreCarryBits(() -> pairs));
        });
  }

}
