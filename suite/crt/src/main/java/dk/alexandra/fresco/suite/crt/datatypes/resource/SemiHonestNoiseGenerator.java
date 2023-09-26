package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.*;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTCombinedPad;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.tools.commitment.CoinTossingComputation;
import dk.alexandra.fresco.tools.commitment.HashBasedCommitmentSerializer;

import java.util.ArrayList;
import java.util.List;

public class SemiHonestNoiseGenerator<ResourcePoolL extends NumericResourcePool, ResourcePoolR extends NumericResourcePool>
        extends NoiseGenerator<ResourcePoolL, ResourcePoolR> {

  private final int batchSize;
  private final int securityParam;
  private final Drng localDrng;
  private final PairShareGenerator<ResourcePoolL, ResourcePoolR> pairShareGenerator;
  private final PaddingGenerator padGenerator;
  private final CoinTossingComputation coinToss;

  public SemiHonestNoiseGenerator(int batchSize, int securityParam) {
    this(batchSize, securityParam, new AesCtrDrbg());
  }

  public SemiHonestNoiseGenerator(int batchSize, int securityParam, Drbg localDrbg) {
    this.batchSize = batchSize;
    this.securityParam = securityParam;
    this.localDrng = new DrngImpl(localDrbg);
    HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();
    this.pairShareGenerator = new PairShareGenerator<>( batchSize, securityParam, new DrngImpl(localDrbg));
    this.padGenerator = new PaddingGenerator(batchSize, securityParam, new DrngImpl(localDrbg));
    this.coinToss = new CoinTossingComputation(32, commitmentSerializer, localDrbg);
  }

  @Override
  public DRes<List<CRTCombinedPad>> buildComputation(ProtocolBuilderNumeric builder,
                                              CRTNumericContext context) {
    return builder.par(par -> {
      DRes<List<CRTSInt>> pairShares = pairShareGenerator.buildComputation(par, context);
      DRes<List<CRTSInt>> rhoPad = padGenerator.buildComputation(par, context);
      DRes<List<CRTSInt>> psiPad = padGenerator.buildComputation(par, context);
      return () -> new Object[] {
              pairShares, rhoPad, psiPad
      };
    }).par((par, data) -> {
      List<CRTCombinedPad> combinedPads = new ArrayList<>(batchSize);
      for (int i = 0; i < batchSize; i++) {
        combinedPads.add(new CRTCombinedPad(
                ((DRes<List<CRTSInt>>) data[0]).out().get(i),
                ((DRes<List<CRTSInt>>) data[1]).out().get(i).getRight(),
                ((DRes<List<CRTSInt>>) data[2]).out().get(i).getRight()));
      }
      return ()->combinedPads;
    });
  }
}
