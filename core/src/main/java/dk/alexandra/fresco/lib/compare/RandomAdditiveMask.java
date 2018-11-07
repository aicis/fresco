package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomBitMask;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RandomAdditiveMask implements
    Computation<RandomBitMask, ProtocolBuilderNumeric> {

  private final int noOfBits;

  public RandomAdditiveMask(int noOfBits) {
    this.noOfBits = noOfBits;
  }

  @Override
  public DRes<RandomBitMask> buildComputation(
      ProtocolBuilderNumeric builder) {
    Numeric numericBuilder = builder.numeric();
    List<DRes<SInt>> bits = new ArrayList<>(noOfBits);
    for (int i = 0; i < noOfBits; i++) {
      DRes<SInt> randomBit = numericBuilder.randomBit();
      bits.add(randomBit);
    }

    List<OInt> twoPows = builder.getOIntArithmetic().getPowersOfTwo(noOfBits);
    AdvancedNumeric innerProductBuilder = builder.advancedNumeric();
    DRes<SInt> value = innerProductBuilder.innerProductWithOInt(twoPows, bits);
    final RandomBitMask randomBitMask = new RandomBitMask(bits, value);
    return () -> randomBitMask;
  }
}
