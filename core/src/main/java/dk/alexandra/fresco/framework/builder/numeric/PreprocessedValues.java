package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

/**
 * Handles values needed internally within FRESCO which preferably should be preprocessed, but a
 * default implementation could handle it online. In other words: protocol suites are encouraged to
 * override the default implementation when implementing {@link BuilderFactoryNumeric}.
 */
public interface PreprocessedValues {

  /**
   * Returns a exponentiation pipe of the form [r^-1, r, r^2, ..., r^pipe_length], where r is a
   * random element in the field of operation.
   * 
   * @param pipeLength The length of the exponentiation pipe. Note that the returned array will be 1
   *        longer than the argument due to the inverse element at position 0.
   * @return An array of the form [r^-1, r, r^2, ..., r^pipe_length]
   */
  DRes<List<DRes<SInt>>> getExponentiationPipe(int pipeLength);
}
