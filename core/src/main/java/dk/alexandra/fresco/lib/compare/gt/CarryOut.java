package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.binary.ArithmeticAndKnownRight;
import dk.alexandra.fresco.lib.math.integer.binary.ArithmeticXorKnownRight;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Given values a and b represented as bits, computes if a + b overflows, i.e., if there is a
 * carry.
 */
public class CarryOut implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<BigInteger>>> clearBits;
  private final DRes<List<DRes<SInt>>> secretBits;
  private final BigInteger carryIn;

  public CarryOut(DRes<List<DRes<BigInteger>>> clearBits, DRes<List<DRes<SInt>>> secretBits,
      BigInteger carryIn) {
    this.secretBits = secretBits;
    this.clearBits = clearBits;
    this.carryIn = carryIn;
  }

  public CarryOut(DRes<List<DRes<BigInteger>>> clearBits, DRes<List<DRes<SInt>>> secretBits) {
    this(clearBits, secretBits, BigInteger.ZERO);
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    // TODO both calls should be in parallel
    // TODO don't re-use generic protocols to cut number of mults in half
    DRes<List<DRes<SInt>>> xoredDef = builder
        .par(new ArithmeticXorKnownRight(secretBits, clearBits));
    DRes<List<DRes<SInt>>> andedDef = builder
        .par(new ArithmeticAndKnownRight(secretBits, clearBits));
    DRes<List<DRes<SIntPair>>> pairs = builder.seq(seq -> {
      List<DRes<SInt>> xored = xoredDef.out();
      List<DRes<SInt>> anded = andedDef.out();
      List<DRes<SIntPair>> innerPairs = new ArrayList<>(xored.size());
      for (int i = 0; i < xored.size() - 1; i++) {
        SIntPair pair = new SIntPair(xored.get(i), anded.get(i));
        innerPairs.add(() -> pair);
      }
      // need to account for carry-in bit
      int lastIdx = xored.size() - 1;
      DRes<SInt> lastCarryPropagator = seq.numeric().add(
          anded.get(lastIdx),
          seq.numeric().mult(carryIn, xored.get(lastIdx)));
      SIntPair pair = new SIntPair(xored.get(lastIdx), lastCarryPropagator);
      innerPairs.add(() -> pair);
      Collections.reverse(innerPairs);
      return () -> innerPairs;
    });
    return builder.seq(new PreCarryBits(pairs));
  }

}
