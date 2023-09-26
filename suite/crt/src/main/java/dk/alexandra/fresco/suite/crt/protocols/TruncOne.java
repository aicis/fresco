package dk.alexandra.fresco.suite.crt.protocols;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

import java.math.BigInteger;

public class TruncOne<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool> extends CRTComputation<SInt, ResourcePoolA, ResourcePoolB> {

    private final DRes<SInt> value;

    public TruncOne(DRes<SInt> value) {
        this.value = value;
    }

    @Override
    public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder,
                                       CRTNumericContext<ResourcePoolA, ResourcePoolB> context) {

        // The multiplicative inverse of p mod q
        BigInteger n2 = context.getLeftModulus().modInverse(context.getRightModulus());

        return builder.seq(seq -> {
            BigInteger U = BigInteger.valueOf(context.getNoOfParties()); // TODO is it right that it is exactly number of parties?
            DRes<SInt> y = new Truncp<>(value).buildComputation(seq);
            // (U + 1)([x] - p[y] + pU)
            DRes<SInt> temp = seq.numeric().sub(value,
                    seq.numeric().mult(context.getLeftModulus(), y));
            temp = seq.numeric().add(context.getLeftModulus().multiply(U), temp);
            DRes<SInt> xPrime = seq.numeric().mult(U.add(BigInteger.ONE), temp);
            DRes<SInt> w = new Truncp<>(xPrime).buildComputation(seq);
            temp = seq.numeric().sub(y, U);
            return seq.numeric().sub(temp, poly(seq.numeric(), context.getModulus(), U, w));
        });
    }

    private DRes<SInt> poly(Numeric seq, BigInteger m, BigInteger U, DRes<SInt> w) {
        long L = U.longValueExact()+1;
        long M = L+1;
        long iterations = M+M*L+L-1;
        for (int i = 0; i < iterations; i++) {
//todo!!
        }
        return null;
    }

    private DRes<SInt> lagrange(Numeric seq, BigInteger m, DRes<SInt> x, int i, int j) {
        return seq.mult(BigInteger.valueOf(i-j).modInverse(m), seq.sub(x, i));
    }
}
