package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

/**
 * Given a secret shared <i>[x]<sub>p</sub></dub></i> in <i>&#x2124;<sub>p</sub></i>, output <i>[x - ep]<sub>q</sub></i> for some <i>0 &le; e &le; n</i> in  <i>&#x2124;<sub>q</sub></i>.
 */
public class LiftPQProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends
    CRTComputation<SInt, ResourcePoolA, ResourcePoolB> {

  private final DRes<SInt> value;
  private CRTSInt r;

  public LiftPQProtocol(DRes<SInt> value) {
    this.value = value;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder,
      CRTNumericContext context) {
    return builder.seq(seq -> seq.append(new CorrelatedNoiseProtocol<>())).seq((seq, noise) -> {
      this.r = (CRTSInt) noise.out();

      DRes<SInt> xBar = context.getLeft().createNumeric(seq).add(value, r.getLeft());
      return context.getLeft().createNumeric(seq).open(xBar);

    }).seq((seq, xBarOpen) -> {
      DRes<SInt> xBarRight = context.getRight().createNumeric(seq).known(xBarOpen);
      return context.getRight().createNumeric(seq).sub(xBarRight, r.getRight());
    });
  }

}
