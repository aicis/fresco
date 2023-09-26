package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTNativeProtocol;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;

import java.math.BigInteger;

public class CRTMult extends CRTNativeProtocol<SInt, NumericResourcePool, NumericResourcePool> {

    private final DRes<SInt> a;
    private final DRes<SInt> b;
    private CRTSInt out;
    private EvaluationStatus status = EvaluationStatus.HAS_MORE_ROUNDS;
    private SpdzMultProtocol spdzMultProtocolLeft;
    private SpdzMultProtocol spdzMultProtocolRight;

    public CRTMult(DRes<SInt> a, DRes<SInt> b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public CRTSInt out() {
        return out;
    }

    @Override
    public EvaluationStatus evaluate(int round, CRTResourcePool CRTResourcePool,
                                     Network network) {
        if (round == 0) {
            CRTSInt aOut = (CRTSInt) a.out();
            DRes<SInt> aLeft = aOut.getLeft();
            DRes<SInt> aRight = aOut.getRight();

            CRTSInt bOut = (CRTSInt) b.out();
            DRes<SInt> bLeft = bOut.getLeft();
            DRes<SInt> bRight = bOut.getRight();

            spdzMultProtocolLeft = new SpdzMultProtocol(aLeft, bLeft);
            spdzMultProtocolRight = new SpdzMultProtocol(aRight, bRight);
        }
        spdzMultProtocolLeft.evaluate(round, (SpdzResourcePool) CRTResourcePool.getSubResourcePools().getFirst(), network);
        status = spdzMultProtocolRight.evaluate(round, (SpdzResourcePool) CRTResourcePool.getSubResourcePools().getSecond(), network);
        if (status == EvaluationStatus.IS_DONE) {
            this.out = new CRTSInt(spdzMultProtocolLeft.out(), spdzMultProtocolRight.out());
            return EvaluationStatus.IS_DONE;
        } else {
            return EvaluationStatus.HAS_MORE_ROUNDS;
        }
    }
}
