package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

/**
 * Operators for converting between different representations of secret values (for instance between
 * an arithmetic representation and boolean representation). <p>NOTE: this is only experimental and
 * will change in the feature. Furthermore, this is currently only supported by Spdz2k, not
 * Spdz.</p>
 */
public interface Conversion extends ComputationDirectory {

  /**
   * Convert from arithmetic representation to boolean representation.
   */
  DRes<SInt> toBoolean(DRes<SInt> arithmeticValue);

  /**
   * Convert from boolean representation to arithmetic representation.
   */
  DRes<SInt> toArithmetic(DRes<SInt> booleanValue);

  /**
   * Convert multiple values from arithmetic to boolean.
   */
  DRes<List<DRes<SInt>>> toBooleanBatch(DRes<List<DRes<SInt>>> arithmeticBatch);

  /**
   * Convert multiple values from boolean to arithmetic.
   */
  DRes<List<DRes<SInt>>> toArithmeticBatch(DRes<List<DRes<SInt>>> booleanBatch);

}
