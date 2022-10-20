package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.Util;
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
      CRTNumericContext<ResourcePoolA, ResourcePoolB> context) {
    return builder.seq(seq -> seq.append(new CorrelatedNoiseProtocol<>())).seq((seq, noise) -> {
      this.r = (CRTSInt) noise.out();

      // Add noise to the right value. The left is ignored.
      DRes<SInt> xBar = context.rightNumeric(seq).add(((CRTSInt) value.out()).getRight(), r.getRight());
      CRTSInt output = new CRTSInt(context.leftNumeric(seq).known(0), xBar);
      return seq.numeric().open(output); // TODO: We only need to open the right, so we should create an openRight function

        // TODO: We haven't added the noise (c from the protocol)

    }).seq((seq, xBarOpen) -> {
      Numeric left = context.leftNumeric(seq);

      // Extract the right value from xBar
      DRes<SInt> xBarLeft = left.known(Util.mapToCRT(xBarOpen, context.getLeftModulus(), context.getRightModulus()).getSecond());

      // Remove the noise and return
      DRes<SInt> adjusted = left.sub(xBarLeft, r.getLeft());
      return new CRTSInt(adjusted, ((CRTSInt) value.out()).getRight());
    });
  }

}
