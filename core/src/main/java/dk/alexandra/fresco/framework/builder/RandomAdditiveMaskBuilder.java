package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public interface RandomAdditiveMaskBuilder {

  Computation<RandomAdditiveMask> additiveMask(int noOfBits);

  class RandomAdditiveMask {

    public final List<SInt> bits;
    public final SInt r;

    public RandomAdditiveMask(List<SInt> bits, SInt r) {
      this.bits = bits;
      this.r = r;
    }
  }
}
