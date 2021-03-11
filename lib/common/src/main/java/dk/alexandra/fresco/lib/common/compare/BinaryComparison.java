package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

/**
 * This computation directory knows about comparing numbers represented in binary form. A default
 * implementation based only on {@link Binary} exists, but protocol suites might implement a more
 * efficient version.
 */
public interface BinaryComparison extends ComputationDirectory {

  /**
   * Create a new BinaryComparison using the given builder.
   *
   * @param builder The root builder to use.
   * @return A new BinaryComparison computation directory.
   */
  static BinaryComparison using(ProtocolBuilderBinary builder) {
    return new DefaultBinaryComparison(builder);
  }

  /**
   * Performs a greater than operation on two bit strings which each represents a number
   * in base 2.
   * 
   * @param inLeft The left secret shared bit string
   * @param inRight The right secret shared bit string
   * @return A deferred result computing inLeft &gt; inRight
   */
  DRes<SBool> greaterThan(List<DRes<SBool>> inLeft, List<DRes<SBool>> inRight);

  /**
   * Performs an equality operation on two bit strings.
   * 
   * @param inLeft The left secret shared bit string
   * @param inRight The right secret shared bit string
   * @return A deferred result computing inLeft == inRight
   */
  DRes<SBool> equal(List<DRes<SBool>> inLeft, List<DRes<SBool>> inRight);
}
