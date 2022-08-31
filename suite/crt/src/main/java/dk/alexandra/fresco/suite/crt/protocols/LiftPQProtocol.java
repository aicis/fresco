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
      CRTNumericContext<ResourcePoolA, ResourcePoolB> context) {
    return builder.seq(seq -> seq.append(new CorrelatedNoiseProtocol<>())).seq((seq, noise) -> {
      this.r = (CRTSInt) noise.out();

      DRes<SInt> xBar;
      if (value.out() instanceof CRTSInt) {
        xBar = seq.append(context.getLeftProtocolSupplier().add(((CRTSInt) value.out()).getLeft(), r.getLeft()));
      } else {
         xBar = seq.append(context.getLeftProtocolSupplier().add(value, r.getLeft()));
      }
      return seq.append(context.getLeftProtocolSupplier().open(xBar));

    }).seq((seq, xBarOpen) -> {
      DRes<SInt> xBarRight = context.getRightProtocolSupplier().known(xBarOpen);
      DRes<SInt> xRight = context.getRightProtocolSupplier().sub(xBarRight, r.getRight());
      return new CRTSInt(null, xRight);
    });
  }

}
