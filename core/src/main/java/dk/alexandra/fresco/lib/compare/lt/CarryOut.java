package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.ArithmeticAndKnownRight;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given values a and b represented as bits, computes if a + b overflows, i.e., if the addition
 * results in a carry.
 */
public class CarryOut implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<OInt>>> openBitsDef;
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
  public CarryOut(DRes<List<DRes<OInt>>> clearBits, DRes<List<DRes<SInt>>> secretBits,
      BigInteger carryIn) {
    this.secretBitsDef = secretBits;
    this.openBitsDef = clearBits;
    this.carryIn = carryIn;
  }

  /**
   * Default call to {@link #CarryOut(DRes, DRes, BigInteger)} without a carry-in.
   */
  public CarryOut(DRes<List<DRes<OInt>>> clearBits, DRes<List<DRes<SInt>>> secretBits) {
    this(clearBits, secretBits, BigInteger.ZERO);
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    OIntFactory oIntFactory = builder.getOIntFactory();
    List<DRes<SInt>> secretBits = secretBitsDef.out();
    List<DRes<OInt>> openBits = openBitsDef.out();
    if (secretBits.size() != openBits.size()) {
      throw new IllegalArgumentException("Number of bits must be the same");
    }
    return builder.par(new ArithmeticAndKnownRight(secretBitsDef, openBitsDef))
        .par((par, andedBits) -> {
          List<DRes<SIntPair>> pairs = new ArrayList<>(andedBits.size());
          for (int i = 0; i < secretBits.size(); i++) {
            DRes<SInt> leftBit = secretBits.get(i);
            DRes<OInt> rightBit = openBits.get(i);
            DRes<SInt> andedBit = andedBits.get(i);
            // TODO we need a logical computation directory for logical ops on arithmetic values
            // logical xor of two bits is leftBit + rightBit - 2 * leftBit * rightBit
            DRes<SInt> xoredBit = par.seq(seq -> {
              Numeric nb = seq.numeric();
              return nb.sub(
                  nb.addOpen(rightBit, leftBit),
                  nb.multByOpen(oIntFactory.two(), andedBit)
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
              seq.numeric().multByOpen(oIntFactory.fromBigInteger(carryIn), lastPair.getFirst()));
          pairs.set(lastIdx, () -> new SIntPair(lastPair.getFirst(), lastCarryPropagator));
          Collections.reverse(pairs);
          return seq.seq(new PreCarryBits(() -> pairs));
        });
  }

}
