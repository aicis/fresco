package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTNativeProtocol;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol;

import java.math.BigInteger;

public class CRTKnown  extends CRTNativeProtocol<SInt, NumericResourcePool, NumericResourcePool> {

    private final BigInteger left;
    private final BigInteger right;
    private CRTSInt out;
    private EvaluationStatus status = EvaluationStatus.HAS_MORE_ROUNDS;
    private SpdzKnownSIntProtocol spdzLeft;
    private SpdzKnownSIntProtocol spdzRight;

    public CRTKnown(Pair<BigInteger, BigInteger> input) {
        this.left = input.getFirst();
        this.right = input.getSecond();
    }

    @Override
    public CRTSInt out() {
        return out;
    }

    @Override
    public EvaluationStatus evaluate(int round, CRTResourcePool CRTResourcePool,
                                     Network network) {
        if (round == 0) {
            spdzLeft = new SpdzKnownSIntProtocol(left);
            spdzRight = new SpdzKnownSIntProtocol(right);
        }
        spdzLeft.evaluate(round, (SpdzResourcePool) CRTResourcePool.getSubResourcePools().getFirst(), network);
        status = spdzRight.evaluate(round, (SpdzResourcePool) CRTResourcePool.getSubResourcePools().getSecond(), network);
        if (status == EvaluationStatus.IS_DONE) {
            this.out = new CRTSInt(spdzLeft.out(), spdzRight.out());
            return EvaluationStatus.IS_DONE;
        } else {
            return EvaluationStatus.HAS_MORE_ROUNDS;
        }
    }
}
