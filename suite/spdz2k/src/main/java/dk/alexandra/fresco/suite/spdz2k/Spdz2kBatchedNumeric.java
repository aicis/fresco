package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.batched.Spdz2kBatchedMultiplication;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.batched.Spdz2kBatchedOutputToAll;
import java.math.BigInteger;

/**
 * Native numeric computation directory which uses batched native protocols for networked native
 * protocols.
 */
public class Spdz2kBatchedNumeric<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNumeric<PlainT> {

  private Spdz2kBatchedMultiplication<PlainT> multiplications;
  private Spdz2kBatchedOutputToAll<PlainT> outputToAll;

  Spdz2kBatchedNumeric(ProtocolBuilderNumeric builder,
      CompUIntFactory<PlainT> factory) {
    super(builder, factory);
  }

  @Override
  public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
    if (multiplications == null) {
      multiplications = new Spdz2kBatchedMultiplication<>();
      builder.append(multiplications);
    }
    return multiplications.append(a, b);
  }

  @Override
  public DRes<BigInteger> open(DRes<SInt> secretShare) {
    if (outputToAll == null) {
      outputToAll = new Spdz2kBatchedOutputToAll<>();
      builder.append(outputToAll);
    }
    return outputToAll.append(secretShare);
  }

}
