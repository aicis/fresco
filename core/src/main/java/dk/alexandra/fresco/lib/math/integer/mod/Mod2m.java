package dk.alexandra.fresco.lib.math.integer.mod;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Computes modular reduction of value mod 2^m.
 */
public class Mod2m implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> value;
  private final int m;

  /**
   * Constructs new {@link Mod2m}.
   *
   * @param value value to reduce
   * @param m exponent (2^{m})
   */
  public Mod2m(DRes<SInt> value, int m) {
    this.value = value;
    this.m = m;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    // TODO implement
    return null;
  }

}
