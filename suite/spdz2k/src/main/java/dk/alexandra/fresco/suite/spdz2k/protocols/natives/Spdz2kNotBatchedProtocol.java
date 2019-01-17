package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.List;

public class Spdz2kNotBatchedProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<List<DRes<SInt>>, PlainT> {

  private final DRes<List<DRes<SInt>>> bits;
  private List<DRes<SInt>> result;

  public Spdz2kNotBatchedProtocol(DRes<List<DRes<SInt>>> bits) {
    this.bits = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    PlainT secretSharedKey = resourcePool.getDataSupplier().getSecretSharedKey();
    List<DRes<SInt>> bitsOut = bits.out();
    this.result = new ArrayList<>(bitsOut.size());
    for (DRes<SInt> secretBit : bitsOut) {
      Spdz2kSIntBoolean<PlainT> notBit = factory.toSpdz2kSIntBoolean(secretBit)
          .xorOpen(factory.one().toBitRep(), secretSharedKey, factory.zero().toBitRep(),
              resourcePool.getMyId() == 1);
      result.add(notBit);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SInt>> out() {
    return result;
  }

}
