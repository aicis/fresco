package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

/**
 * Given a secret shared <i>[x]<sub>q</sub></dub></i> in <i>&#x2124;<sub>q</sub></i> with <i>x < q - np</i>, output <i>[x]<sub>p</sub></i> in <i>&#x2124;<sub>p</sub></i>.
 */
public class LiftQPProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends
    CRTComputation<SInt, ResourcePoolA, ResourcePoolB> {

  private final DRes<SInt> value;
  private CRTSInt r;

  public LiftQPProtocol(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder,
      CRTNumericContext context) {
    return builder.seq(seq -> new CorrelatedNoiseProtocol<>()).seq((seq, noise) -> {
      this.r = (CRTSInt) noise.out();
      DRes<SInt> xBar = context.getRight().createNumeric(seq).add(value, r.getRight());
      return context.getRight().createNumeric(seq).open(xBar);
    }).seq((seq, xBarOpen) -> {
      DRes<SInt> xBarRight = context.getLeft().createNumeric(seq).known(xBarOpen);
      return context.getLeft().createNumeric(seq).sub(xBarRight, r.getLeft());
    });
  }

}
