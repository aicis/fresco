package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.tools.commitment.CoinTossingComputation;
import dk.alexandra.fresco.tools.commitment.HashBasedCommitmentSerializer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CovertNoiseGenerator<ResourcePoolL extends NumericResourcePool, ResourcePoolR extends NumericResourcePool>
        extends NoiseGenerator<ResourcePoolL, ResourcePoolR> {

  private final int batchSize;
  private final int deterrenceFactor;
  private final int securityParam;

  private final SemiHonestNoiseGenerator<ResourcePoolL, ResourcePoolR> noiseGenerator;
  private final CoinTossingComputation coinToss;
  private AesCtrDrbg jointDrbg;


  public CovertNoiseGenerator(int batchSize, int deterrenceFactor, int securityParam) {
    this.batchSize = batchSize;
    this.deterrenceFactor = deterrenceFactor;
    this.securityParam = securityParam;

    AesCtrDrbg localDrbg = new AesCtrDrbg();
    HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();
    this.noiseGenerator = new SemiHonestNoiseGenerator<>(this.deterrenceFactor * batchSize);
    this.coinToss = new CoinTossingComputation(32, commitmentSerializer, localDrbg);
  }

  @Override
  public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder,
                                              CRTNumericContext<ResourcePoolL, ResourcePoolR> context) {
    return builder.par(noiseGenerator::buildComputation).par((par, honestNoisePairs) -> {
      // Sample batchsize random noise pairs to keep, mark all other to be checked/selected
      DRes<byte[]> seed = null;
      if (this.jointDrbg == null) {
        seed = coinToss.buildComputation(par);
      }  // reuse seed if already computed.

      return Pair.lazy(seed, honestNoisePairs);
    }).par((par, pair) -> { // Open selected pairs
      // prepare pairs indices to keep
      byte[] seed = pair.getFirst().out();
      if (this.jointDrbg == null) {
        jointDrbg = new AesCtrDrbg(seed);
      }

      int[] toKeep = new int[batchSize];
      for (int i = 0; i < batchSize; i++) {
        byte[] sample = new byte[securityParam + Integer.numberOfLeadingZeros(deterrenceFactor)];
        jointDrbg.nextBytes(sample);
        toKeep[i] = new BigInteger(1, sample).mod(BigInteger.valueOf(deterrenceFactor)).intValue();
      }

      // check all other pairs
      List<CRTSInt> honestNoisePairs = pair.getSecond();
      int i, j = 0;
      List<Pair<DRes<BigInteger>, DRes<BigInteger>>> testSet = new ArrayList<Pair<DRes<BigInteger>, DRes<BigInteger>>>();
      for (CRTSInt noisePair : honestNoisePairs) {
        i = j / deterrenceFactor; // current batch no.
        if (j == toKeep[i] + i * batchSize) // skip the once marked to keep.
          // scale them back up, so they correspond to a batch.
          continue;
        j++;
        DRes<SInt> leftClosed = noisePair.getLeft();
        DRes<SInt> rightClosed = noisePair.getRight();
        DRes<BigInteger> leftOpened = context.leftNumeric(par).open(leftClosed);
        DRes<BigInteger> rightOpened = context.rightNumeric(par).open(rightClosed);
        testSet.add(new Pair<>(leftOpened, rightOpened));
      }

      // filter out the ones we need.
      List<CRTSInt> noisePairs = new ArrayList<>(batchSize);
      for (i = 0; i < batchSize; i++) {
        int k = toKeep[i] + i * deterrenceFactor;
        noisePairs.add(honestNoisePairs.get(k));
      }
      return Pair.lazy(noisePairs, testSet);

    }).par((par, pair) -> { // Check selected pairs

      List<Pair<DRes<BigInteger>, DRes<BigInteger>>> testSet = pair.getSecond();
      for (Pair<DRes<BigInteger>, DRes<BigInteger>> noisePair : testSet) {
        BigInteger rp = noisePair.getFirst().out();
        BigInteger rq = noisePair.getFirst().out();
        if (rp.compareTo(rq) != 0) {
          throw new MaliciousException("Cheating party, terminating");
        }
      }
      List<CRTSInt> noisePairs = pair.getFirst();
      return DRes.of(noisePairs);
    });
  }
}