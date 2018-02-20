package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class DecimalBasicFixedNumeric extends BasicFixedNumeric {

  DecimalBasicFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder, 10, 4, 16);
  }

}
