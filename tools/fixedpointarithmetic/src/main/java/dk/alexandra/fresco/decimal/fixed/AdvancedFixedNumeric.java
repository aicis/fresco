package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class AdvancedFixedNumeric extends DefaultAdvancedRealNumeric {

  public AdvancedFixedNumeric(ProtocolBuilderNumeric builder, FixedNumeric.Base base) {
    super(builder, scope -> new FixedNumeric(scope, base));
  }

}
