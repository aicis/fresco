package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PaddingGenerator //implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {
        <ResourcePoolL extends NumericResourcePool, ResourcePoolR extends NumericResourcePool>
        extends CRTComputation<List<CRTSInt>, ResourcePoolL, ResourcePoolR> {

    private final int batchSize;
    private final int securityParam;
    private final Drng localDrng;

    public PaddingGenerator(int batchSize, int securityParam) {
        this(batchSize, securityParam, new DrngImpl(new AesCtrDrbg()));
    }

    public PaddingGenerator(int batchSize, int securityParam, Drng localDrng) {
        this.batchSize = batchSize;
        this.securityParam = securityParam;
        this.localDrng = localDrng;
    }

    @Override
    public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder, CRTNumericContext<ResourcePoolL, ResourcePoolR> context) {
        return builder.par(par -> {
            Numeric right = context.rightNumeric(par);
            List<CRTSInt> res = new ArrayList<>(batchSize);
            for (int i = 0; i < batchSize; i++) {
                for (int j = 1; j <= par.getBasicNumericContext().getNoOfParties(); j++) {
                    DRes<SInt> curPad;
                    if (j == par.getBasicNumericContext().getMyId()) {
                        BigInteger modulo = BigInteger.valueOf(2).pow(securityParam);
                        BigInteger myRhoShare = localDrng.nextBigInteger(modulo);
                        curPad = right.input(myRhoShare, par.getBasicNumericContext().getMyId());
                    } else {
                        curPad = right.input(null, j);
                    }
                    res.add(new CRTSInt(null, curPad));
                }
            }
            return () -> res;
        });
    }
}