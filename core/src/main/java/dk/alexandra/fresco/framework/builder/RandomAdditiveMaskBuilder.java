package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public interface RandomAdditiveMaskBuilder<SIntT extends SInt> {

  Computation<RandomAdditiveMask<SIntT>> additiveMask(int noOfBits);

  class RandomAdditiveMask<SIntT extends SInt> {

    public final List<SIntT> bits;
    public final SIntT r;

    public RandomAdditiveMask(List<SIntT> bits, SIntT r) {
      this.bits = bits;
      this.r = r;
    }
  }
}
