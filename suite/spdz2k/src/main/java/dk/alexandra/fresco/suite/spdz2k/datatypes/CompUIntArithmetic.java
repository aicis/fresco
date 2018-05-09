package dk.alexandra.fresco.suite.spdz2k.datatypes;

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
  private final List<OInt> powersOfTwo;

  public CompUIntArithmetic(CompUIntFactory<CompT> factory) {
    this.factory = factory;
    this.powersOfTwo = initializePowersOfTwo(factory.getLowBitLength());
  }

  @Override
  public OInt one() {
    return factory.one();
  }

  @Override
  public List<OInt> toBits(OInt openValue, int numBits) {
    CompT value = factory.fromOInt(openValue);
    List<OInt> bits = new ArrayList<>(numBits);
    for (int b = 0; b < numBits; b++) {
      bits.add(value.testBitAsUInt(b));
    }
    Collections.reverse(bits);
    return bits;
  }

  @Override
  public List<OInt> getPowersOfTwo(int numPowers) {
    if (numPowers > factory.getLowBitLength()) {
      throw new UnsupportedOperationException();
    } else {
      return powersOfTwo.subList(0, numPowers);
    }
  }

  @Override
  public OInt twoTo(int power) {
    if (power > factory.getLowBitLength()) {
      throw new UnsupportedOperationException();
    } else {
      return powersOfTwo.get(power);
    }
  }

  @Override
  public OInt modTwoTo(OInt input, int power) {
    if (power > factory.getLowBitLength()) {
      throw new UnsupportedOperationException();
    } else {
      return factory.fromOInt(input).clearAboveBitAt(power);
    }
  }

  private List<OInt> initializePowersOfTwo(int numPowers) {
    List<OInt> powers = new ArrayList<>(numPowers);
    CompT current = factory.one();
    final CompT tempOuter = current;
    powers.add(tempOuter);
    for (int i = 1; i < numPowers; i++) {
      current = current.multiply(factory.two());
      final CompT temp = current;
      powers.add(temp);
    }
    return powers;
  }

}
