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
import dk.alexandra.fresco.suite.crt.datatypes.CRTCombinedPad;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SemiHonestNoiseGenerator<ResourcePoolL extends NumericResourcePool, ResourcePoolR extends NumericResourcePool>
        extends NoiseGenerator<ResourcePoolL, ResourcePoolR> {

  private final int batchSize;
  private final int securityParam;
  private final Drng localDrng;

  public SemiHonestNoiseGenerator(int batchSize, int securityParam) {
    this(batchSize, securityParam, new DrngImpl(new AesCtrDrbg()));
  }

  public SemiHonestNoiseGenerator(int batchSize, int securityParam, Drng localDrng) {
    this.batchSize = batchSize;
    this.securityParam = securityParam;
    this.localDrng = localDrng;
  }

  @Override
  public DRes<List<CRTCombinedPad>> buildComputation(ProtocolBuilderNumeric builder,
                                              CRTNumericContext context) {
    return builder.par(par -> {
      Numeric left = context.leftNumeric(par);
      Numeric right = context.rightNumeric(par);
      List<CRTCombinedPad> list = new ArrayList<>(batchSize);
      for (int i = 0; i < batchSize; i++) {
        DRes<SInt> r = left.randomElement();
        CRTSInt noisePair = new CRTSInt(r, r);
        DRes<SInt> rho = getStatisticalShares(right, par.getBasicNumericContext().getMyId(), par.getBasicNumericContext().getNoOfParties());
        DRes<SInt> psi = getStatisticalShares(right, par.getBasicNumericContext().getMyId(), par.getBasicNumericContext().getNoOfParties());
        list.add(new CRTCombinedPad(noisePair, rho, psi));
      }
      return DRes.of(list);
    });
  }

  private DRes<SInt> getStatisticalShares(Numeric right, int myId, int parties) {
    DRes<SInt> sharedRand = right.known(0);
    for (int j = 1; j <= parties; j++) {
      DRes<SInt> curRand;
      if (j == myId) {
        BigInteger modulo = BigInteger.valueOf(2).pow(securityParam);
        BigInteger rand = localDrng.nextBigInteger(modulo);
        curRand = right.input(rand, myId);
      } else {
        curRand = right.input(null, j);
      }
      sharedRand = right.add(sharedRand, curRand);
    }
    return sharedRand;
  }

}
