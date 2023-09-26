package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.OpenedValueStoreImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTNativeProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;

import java.math.BigInteger;

public class CRTOpen extends CRTNativeProtocol<BigInteger, NumericResourcePool, NumericResourcePool> {

    private final DRes<SInt> input;
    private BigInteger out;
    private EvaluationStatus status = EvaluationStatus.HAS_MORE_ROUNDS;
    private SpdzOutputToAllProtocol spdzLeft;
    private SpdzOutputToAllProtocol spdzRight;

    public CRTOpen(DRes<SInt> input) {
        this.input = input;
    }

    @Override
    public BigInteger out() {
        return out;
    }

    @Override
    public EvaluationStatus evaluate(int round, CRTResourcePool CRTResourcePool,
                                     Network network) {
        if (round == 0) {
            spdzLeft = new SpdzOutputToAllProtocol(((CRTSInt) input.out()).getLeft());
            spdzRight = new SpdzOutputToAllProtocol(((CRTSInt) input.out()).getRight());
        }
        spdzLeft.evaluate(round, (SpdzResourcePool) CRTResourcePool.getSubResourcePools().getFirst(), network);
        status = spdzRight.evaluate(round, (SpdzResourcePool) CRTResourcePool.getSubResourcePools().getSecond(), network);
        if (status == EvaluationStatus.IS_DONE) {
            Pair<FieldDefinition,FieldDefinition> fieldDefs = CRTResourcePool.getFieldDefinitions();
            this.out = Util.mapToBigInteger(spdzLeft.out(), spdzRight.out(), fieldDefs.getFirst().getModulus(), fieldDefs.getSecond().getModulus());
            return EvaluationStatus.IS_DONE;
        } else {
            return EvaluationStatus.HAS_MORE_ROUNDS;
        }
    }
}

