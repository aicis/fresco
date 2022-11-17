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

import java.math.BigInteger;

/**
 * Given a secret shared <i>([_]<sub>p</sub>, [x]<sub>q</sub>)</dub></i> in <i>&#x2124;<sub>p</sub></i>, output <i>([x]<sub>p</sub>, [x]<sub>q</sub>)</i>.
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
        //q' = q / 2 is divisible by p, so adding it to the input only affects the output by q mod p = 1 if there's an overflow.
        BigInteger qPrime = new BigInteger("3138550867693340351802905239100779285196644626743924002860");

        DRes<SInt> noise = new CorrelatedNoiseProtocol<>(builder);
        return builder.seq(seq -> {
            this.r = (CRTSInt) noise.out();
            return seq.numeric().add(qPrime, value);
        }).seq((seq, value) -> {

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
