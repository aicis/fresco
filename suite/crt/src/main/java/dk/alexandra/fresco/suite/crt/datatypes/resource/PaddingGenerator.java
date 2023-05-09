package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTPadPair;
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
    private final Drbg localDrbg;

    public PaddingGenerator(int batchSize, int securityParam) {
        this(batchSize, securityParam, new AesCtrDrbg());
    }

    public PaddingGenerator(int batchSize, int securityParam, Drbg localDrbg) {
        this.batchSize = batchSize;
        this.securityParam = securityParam;
        this.localDrbg = localDrbg;
    }

    @Override
    public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder, CRTNumericContext<ResourcePoolL, ResourcePoolR> context) {
        return builder.par(par -> {
            Numeric right = context.rightNumeric(par);
            List<CRTSInt> res = new ArrayList<>(batchSize);
            for (int i = 0; i < batchSize; i++) {
//                DRes<SInt> sharedRho = par.numeric().known(0);
                for (int j = 1; j <= par.getBasicNumericContext().getNoOfParties(); j++) {
                    DRes<SInt> curRho;
                    if (j == par.getBasicNumericContext().getMyId()) {
                        BigInteger modulo = BigInteger.valueOf(2).pow(securityParam);
                        byte[] sample = new byte[1 + (securityParam / 8)];
                        localDrbg.nextBytes(sample);
                        BigInteger myRhoShare = new BigInteger(1, sample).mod(modulo);
                        curRho = right.input(myRhoShare, par.getBasicNumericContext().getMyId());
                    } else {
                        curRho = right.input(null, j);
                    }
                    res.add(new CRTSInt(null, curRho));
//                    res.put(j, curRho);
//                    sharedRho = par.numeric().add(sharedRho, curRho);
                }
//                res.add(sharedRho);
            }
            return () -> res;
//            return () -> res.stream().map(DRes::out).collect(Collectors.toList());
//        }).par((par, data) -> {
//            Numeric right = context.rightNumeric(par);
//            List<CRTSInt> res = new ArrayList<>(batchSize);
//            for (int i = 0; i < batchSize; i++) {
//                DRes<SInt> sharedRho = right.known(0);
//                for (DRes<SInt> cur: data.get(i)) {
//                    sharedRho = right.add(sharedRho, cur);
//                }
//                res.add(new CRTSInt(null, sharedRho));
//            }
//            return ()-> res;
        });
    }
}