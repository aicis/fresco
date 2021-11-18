package dk.alexandra.fresco.lib.fixed.truncations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public interface Truncation {

  BigInteger getDivisor();

  DRes<SInt> truncate(DRes<SInt> value, ProtocolBuilderNumeric scope);

}
