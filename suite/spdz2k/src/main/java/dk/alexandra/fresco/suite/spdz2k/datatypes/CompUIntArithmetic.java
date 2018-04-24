package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.OIntArithmetic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link OIntArithmetic} for {@link CompT}.
 */
public class CompUIntArithmetic<CompT extends CompUInt<?, ?, CompT>> implements OIntArithmetic {

  private final CompUIntFactory<CompT> factory;
  private final List<DRes<OInt>> powersOfTwo;

  public CompUIntArithmetic(CompUIntFactory<CompT> factory) {
    this.factory = factory;
    this.powersOfTwo = initializePowersOfTwo(factory.getLowBitLength());
  }

  @Override
  public List<DRes<OInt>> toBits(OInt openValue, int numBits) {
    CompUInt value = (CompUInt) openValue;
    List<DRes<OInt>> bits = new ArrayList<>(numBits);
    for (int b = 0; b < numBits; b++) {
      boolean boolBit = value.testBit(b);
      OInt bit = boolBit ? factory.one() : factory.zero();
      bits.add(() -> bit);
    }
    Collections.reverse(bits);
    return bits;
  }

  @Override
  public List<DRes<OInt>> getPowersOfTwo(int numPowers) {
    if (numPowers > factory.getLowBitLength()) {
      throw new UnsupportedOperationException();
    } else {
      return powersOfTwo.subList(0, numPowers);
    }
  }

  @Override
  public DRes<OInt> twoTo(int power) {
    if (power > factory.getLowBitLength()) {
      throw new UnsupportedOperationException();
    } else {
      return powersOfTwo.get(power);
    }
  }

  private List<DRes<OInt>> initializePowersOfTwo(int numPowers) {
    List<DRes<OInt>> powers = new ArrayList<>(numPowers);
    CompT current = factory.one();
    final CompT tempOuter = current;
    powers.add(() -> tempOuter);
    for (int i = 1; i < numPowers; i++) {
      current = current.multiply(factory.two());
      final CompT temp = current;
      powers.add(() -> temp);
    }
    return powers;
  }

}
