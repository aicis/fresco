package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.Objects;

/**
 * Generic native Spdz2k protocol.
 */
public abstract class Spdz2kNativeProtocol<
    OutputT,
    PlainT extends CompUInt<?, ?, PlainT>> implements
    NativeProtocol<OutputT, Spdz2kResourcePool<PlainT>> {

  /**
   * Get result from deferred and downcast result to {@link Spdz2kSInt<PlainT>}.
   */
  Spdz2kSInt<PlainT> toSpdz2kSInt(DRes<SInt> value) {
    return Objects.requireNonNull((Spdz2kSInt<PlainT>) value.out());
  }

}
