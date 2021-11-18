package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.crt.Util;
import java.math.BigInteger;

public class CRTBigIntegerProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool>
    extends CRTAbstractProtocol<BigInteger, BigInteger, BigInteger, ResourcePoolA, ResourcePoolB> {

  public CRTBigIntegerProtocol(NativeProtocol<BigInteger, ResourcePoolA> protocolA,
      NativeProtocol<BigInteger, ResourcePoolB> protocolB, BigInteger p, BigInteger q) {
    super(protocolA, protocolB, (a, b) -> Util.mapToBigInteger(a, b, p, q));
  }

}
