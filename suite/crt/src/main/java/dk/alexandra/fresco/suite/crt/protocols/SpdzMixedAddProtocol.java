package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTRingDefinition;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;

public class SpdzMixedAddProtocol implements
    NativeProtocol<BigInteger, CRTResourcePool<SpdzResourcePool, SpdzResourcePool>> {

  private final CRTSInt value;

  public SpdzMixedAddProtocol(DRes<SInt> xp, DRes<SInt> xq) {
    this.value = new CRTSInt(xp, xq);
  }

  public SpdzMixedAddProtocol(DRes<SInt> value) {
    this.value = (CRTSInt) value.out();
  }

  @Override
  public EvaluationStatus evaluate(int round,
      CRTResourcePool<SpdzResourcePool, SpdzResourcePool> resourcePool, Network network) {
    // TODO
    return null;
  }

  @Override
  public BigInteger out() {
    return null;
  }

}
