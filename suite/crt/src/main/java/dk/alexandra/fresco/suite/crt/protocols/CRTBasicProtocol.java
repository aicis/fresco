package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

public class CRTBasicProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool>
    extends CRTAbstractProtocol<SInt, SInt, SInt, ResourcePoolA, ResourcePoolB> {

  public CRTBasicProtocol(NativeProtocol<SInt, ResourcePoolA> protocolA, NativeProtocol<SInt, ResourcePoolB> protocolB) {
    super(protocolA, protocolB, CRTSInt::new);
  }

}
