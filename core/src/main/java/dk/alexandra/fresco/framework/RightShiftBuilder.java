package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public interface RightShiftBuilder<SIntT extends SInt> {

  /**
   * @param input input
   * @return result input >> 1
   */
  Computation<SIntT> rightShift(Computation<SIntT> input);

  /**
   * @param input input
   * @return result: input >> 1<br> remainder: The <code>shifts</code> least significant bits of the
   * input with the least significant having index 0.
   */
  Computation<RightShiftResult<SIntT>> rightShiftWithRemainder(Computation<SIntT> input);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return result input >> shifts
   */
  Computation<SIntT> rightShift(Computation<SIntT> input, int shifts);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return result: input >> shifts<br> remainder: The <code>shifts</code> least significant bits
   * of the input with the least significant having index 0.
   */
  Computation<RightShiftResult<SIntT>> rightShiftWithRemainder(Computation<SIntT> input,
      int shifts);

  /**
   * result input >> 1
   * remainder the least significant bit of input
   */
  class RightShiftResult<SIntT extends SInt> {

    final Computation<SIntT> result;
    final List<Computation<SIntT>> remainder;

    public RightShiftResult(Computation<SIntT> result,
        List<Computation<SIntT>> remainder) {
      this.result = result;
      this.remainder = remainder;
    }

    public Computation<SIntT> getResult() {
      return result;
    }

    public List<Computation<SIntT>> getRemainder() {
      return remainder;
    }
  }
}
