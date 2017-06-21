package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public interface RightShiftBuilder {

  /**
   * @param input input
   * @return result input >> 1
   */
  Computation<SInt> rightShift(Computation<SInt> input);

  /**
   * @param input input
   * @return result: input >> 1<br> remainder: The <code>shifts</code> least significant bits of the
   * input with the least significant having index 0.
   */
  Computation<RightShiftResult> rightShiftWithRemainder(Computation<SInt> input);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return result input >> shifts
   */
  Computation<SInt> rightShift(Computation<SInt> input, int shifts);

  /**
   * @param input input
   * @param shifts Number of shifts
   * @return result: input >> shifts<br> remainder: The <code>shifts</code> least significant bits
   * of the input with the least significant having index 0.
   */
  Computation<RightShiftResult> rightShiftWithRemainder(Computation<SInt> input,
      int shifts);

  /**
   * result input >> 1
   * remainder the least significant bit of input
   */
  class RightShiftResult {

    final SInt result;
    final List<SInt> remainder;

    public RightShiftResult(SInt result,
        List<SInt> remainder) {
      this.result = result;
      this.remainder = remainder;
    }

    public SInt getResult() {
      return result;
    }

    public List<SInt> getRemainder() {
      return remainder;
    }
  }
}
