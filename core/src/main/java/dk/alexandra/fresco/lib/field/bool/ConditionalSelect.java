package dk.alexandra.fresco.lib.field.bool;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Binary conditional select. Returns <code> selector ? a : b</code>
 */
public class ConditionalSelect implements Computation<SBool, ProtocolBuilderBinary> {

  private final DRes<SBool> a, b, selector;

  public ConditionalSelect(DRes<SBool> selector, DRes<SBool> a, DRes<SBool> b) {
    this.a = a;
    this.b = b;
    this.selector = selector;
  }

  @Override
  public DRes<SBool> buildComputation(ProtocolBuilderBinary builder) {
    Binary binary = builder.binary();

    DRes<SBool> x = binary.xor(a, b);
    DRes<SBool> y = binary.and(selector, x);
    return binary.xor(y, b);
  }

}
