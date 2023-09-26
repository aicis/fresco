package dk.alexandra.fresco.suite.crt.protocols.framework;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import java.util.function.BiFunction;

/**
 * Abstract CRT protocol which simply forwards evaluations calls to the sub-protocols in the two
 * actual protocol suites
 */
public class CRTAbstractProtocol<OutputA, OutputB, OutputT, ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool>
    extends CRTNativeProtocol<OutputT, ResourcePoolA, ResourcePoolB> {

  private final NativeProtocol<OutputA, ResourcePoolA> protocolA;
  private final NativeProtocol<OutputB, ResourcePoolB> protocolB;
  private final BiFunction<OutputA, OutputB, OutputT> finisher;
  private OutputT output;
  private EvaluationStatus statusA = EvaluationStatus.HAS_MORE_ROUNDS, statusB = EvaluationStatus.HAS_MORE_ROUNDS;

  public CRTAbstractProtocol(NativeProtocol<OutputA, ResourcePoolA> protocolA,
      NativeProtocol<OutputB, ResourcePoolB> protocolB,
      BiFunction<OutputA, OutputB, OutputT> finisher) {
    this.protocolA = protocolA;
    this.protocolB = protocolB;
    this.output = null;
    this.finisher = finisher;
  }

  @Override
  public EvaluationStatus evaluate(int round, CRTResourcePool<ResourcePoolA, ResourcePoolB> rp,
      Network network) {

    if (statusA.equals(EvaluationStatus.HAS_MORE_ROUNDS)) {
      statusA = protocolA.evaluate(round, rp.getSubResourcePools().getFirst(), network);
    }
    if (statusB.equals(EvaluationStatus.HAS_MORE_ROUNDS)) {
      statusB = protocolB.evaluate(round, rp.getSubResourcePools().getSecond(), network);
    }

    if (statusA.equals(EvaluationStatus.IS_DONE) && statusB.equals(EvaluationStatus.IS_DONE)) {
      output = finisher.apply(protocolA.out(), protocolB.out());
      return EvaluationStatus.IS_DONE;
    } else {
      return EvaluationStatus.HAS_MORE_ROUNDS;
    }
  }

  @Override
  public OutputT out() {
    return output;
  }
}
