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
 * Given a secret shared <i>([x]<sub>p</sub>, [_]<sub>q</sub>)</dub></i> in <i>&#x2124;<sub>p</sub></i>, output <i>([x]<sub>p</sub>, [x - ep]<sub>q</sub>)</i> for some <i>0 &le; e &le; n</i> in  <i>&#x2124;<sub>q</sub></i>.
 */
public class LiftPQProtocol<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends
        CRTComputation<SInt, ResourcePoolA, ResourcePoolB> {

    private final DRes<SInt> value;
    private CRTSInt r;

    // The right value is ignored in the input
    public LiftPQProtocol(DRes<SInt> value) {
        this.value = value;
    }

    @Override
    public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder,
                                       CRTNumericContext<ResourcePoolA, ResourcePoolB> context) {
        return builder.seq(new CorrelatedNoiseProtocol<>()).seq((seq, noise) -> {
            this.r = noise;

            // Add noise to the left value. The right is ignored.
            DRes<SInt> xBar = context.leftNumeric(seq).add(((CRTSInt) value.out()).getLeft(), r.getLeft());
            CRTSInt output = new CRTSInt(xBar, context.rightNumeric(seq).known(0));
            return seq.numeric().open(output); // TODO: We only need to open the left, so we should create an openLeft function

        }).seq((seq, xBarOpen) -> {
            Numeric right = context.rightNumeric(seq);

            // Extract the left value from xBar
            DRes<SInt> xBarRight = right.known(Util.mapToCRT(xBarOpen, context.getLeftModulus(), context.getRightModulus()).getFirst());

            // Remove the noise and return
            DRes<SInt> adjusted = right.sub(xBarRight, r.getRight());
            return new CRTSInt(((CRTSInt) value.out()).getLeft(), adjusted);
        });
    }
}
