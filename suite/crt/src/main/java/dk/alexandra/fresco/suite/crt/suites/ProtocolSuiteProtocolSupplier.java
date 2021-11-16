package dk.alexandra.fresco.suite.crt.suites;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public interface ProtocolSuiteProtocolSupplier<ResourcePoolT extends NumericResourcePool> {

  NativeProtocol<SInt, ResourcePoolT> known(BigInteger value);

  NativeProtocol<SInt, ResourcePoolT> input(BigInteger value, int playerId);

  NativeProtocol<BigInteger, ResourcePoolT> open(DRes<SInt> value, int playerId);

  NativeProtocol<BigInteger, ResourcePoolT> open(DRes<SInt> value);

  NativeProtocol<SInt, ResourcePoolT> add(DRes<SInt> a, DRes<SInt> b);

  NativeProtocol<SInt, ResourcePoolT> add(BigInteger a, DRes<SInt> b);

  NativeProtocol<SInt, ResourcePoolT> sub(DRes<SInt> a, DRes<SInt> b);

  NativeProtocol<SInt, ResourcePoolT> sub(DRes<SInt> a, BigInteger b);

  NativeProtocol<SInt, ResourcePoolT> sub(BigInteger a, DRes<SInt> b);

  NativeProtocol<SInt, ResourcePoolT> mult(DRes<SInt> a, DRes<SInt> b);

  NativeProtocol<SInt, ResourcePoolT> mult(BigInteger a, DRes<SInt> b);

  NativeProtocol<SInt, ResourcePoolT> randomElement();

  NativeProtocol<SInt, ResourcePoolT> randomBit();

}
