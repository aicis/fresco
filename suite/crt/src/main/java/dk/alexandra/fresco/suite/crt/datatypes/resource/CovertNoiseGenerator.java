package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTCombinedPad;
import dk.alexandra.fresco.suite.crt.datatypes.CRTNoise;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.tools.commitment.CoinTossingComputation;
import dk.alexandra.fresco.tools.commitment.HashBasedCommitmentSerializer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CovertNoiseGenerator<ResourcePoolL extends NumericResourcePool, ResourcePoolR extends NumericResourcePool>
        extends NoiseGenerator<ResourcePoolL, ResourcePoolR, CRTCombinedPad> {

  private final int batchSize;
  private final int deterrenceFactor;
  private final int securityParam;

  private final SemiHonestNoiseGenerator<ResourcePoolL, ResourcePoolR> noiseGenerator;
  private final PaddingGenerator padGenerator;
  private final CoinTossingComputation coinToss;
  private AesCtrDrbg jointDrbg;
  private Drbg localDrbg;


  public CovertNoiseGenerator(int batchSize, int deterrenceFactor, int securityParam) {
    this(batchSize, deterrenceFactor, securityParam, new AesCtrDrbg());
  }

  public CovertNoiseGenerator(int batchSize, int deterrenceFactor, int securityParam, Drbg localDrbg) {
    this.batchSize = batchSize;
    this.deterrenceFactor = deterrenceFactor;
    this.securityParam = securityParam;

    this.localDrbg = localDrbg;
    HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();
    this.noiseGenerator = new SemiHonestNoiseGenerator<>(this.deterrenceFactor * batchSize);
    this.padGenerator = new PaddingGenerator(this.deterrenceFactor * batchSize, securityParam, localDrbg);
    this.coinToss = new CoinTossingComputation(32, commitmentSerializer, localDrbg);
  }

  @Override
  public DRes<List<CRTCombinedPad>> buildComputation(ProtocolBuilderNumeric builder,
                                                     CRTNumericContext context) {
    return builder.par(par -> {
      DRes<List<CRTNoise>> semiHonestNoise = noiseGenerator.buildComputation(par, context);
      DRes<List<CRTSInt>> rhoPad = padGenerator.buildComputation(par, context);
      DRes<List<CRTSInt>> psiPad = padGenerator.buildComputation(par, context);
      // Sample batchsize random noise pairs to keep, mark all other to be checked/selected
      DRes<byte[]> seed;
      if (this.jointDrbg == null) {
        seed = coinToss.buildComputation(par);
      }  // reuse seed if already computed.
      else {
        seed = null;
      }
      return () -> new Object[] {
              semiHonestNoise, rhoPad, psiPad, seed,
      };
    }).par((par, data) -> {
      List<CRTCombinedPad> combinedPads = new ArrayList<>(securityParam * batchSize);
      for (int i = 0; i < deterrenceFactor * batchSize; i++) {
        combinedPads.add(new CRTCombinedPad(
                ((DRes<List<CRTNoise>>) data[0]).out().get(i).getNoisePair(),
                ((DRes<List<CRTSInt>>) data[1]).out().get(i).getRight(),
                ((DRes<List<CRTSInt>>) data[2]).out().get(i).getRight()));
      }
      return Pair.lazy((DRes<byte[]>) data[3], combinedPads);
    }).par((par, pair) -> { // Open selected pairs
      // prepare pairs indices to keep
      if (this.jointDrbg == null) {
        byte[] seed = pair.getFirst().out();
        jointDrbg = new AesCtrDrbg(seed);
      }

      // Sample indexes of elements to keep
      int[] toKeep = new int[batchSize];
      for (int i = 0; i < batchSize; i++) {
        // We need securityParam + log(deterrenceFactor) bits to ensure uniform randomness
        byte[] sample = new byte[1+((securityParam + Integer.numberOfLeadingZeros(deterrenceFactor))/8)];
        jointDrbg.nextBytes(sample);
        toKeep[i] = new BigInteger(1, sample).mod(BigInteger.valueOf(deterrenceFactor)).intValue();
      }

      // check all other pairs
      List<CRTCombinedPad> combinedPad = pair.getSecond();
      int i = 0;
      List<DRes<BigInteger>> padTestSet = new ArrayList<>();
      List<Pair<DRes<BigInteger>, DRes<BigInteger>>> noisePairTestSet = new ArrayList<>();
      List<CRTSInt> noisePairsToKeep = new ArrayList<>(batchSize);
      List<DRes<SInt>> rhoToKeep = new ArrayList<>(batchSize);
      List<DRes<SInt>> psiToKeep = new ArrayList<>(batchSize);
      for (int j = 0; j < batchSize*deterrenceFactor; j++) {
        i = j / deterrenceFactor; // current batch no.
        if (j == toKeep[i] + i * deterrenceFactor) {// skip the once marked to keep.
          // scale them back up, so they correspond to a batch.
          noisePairsToKeep.add(combinedPad.get(j).getNoisePair());
          rhoToKeep.add(combinedPad.get(j).getRho());
          psiToKeep.add(combinedPad.get(j).getPsi());
        } else {
          DRes<SInt> leftClosed = combinedPad.get(j).getNoisePair().getLeft();
          DRes<SInt> rightClosed = combinedPad.get(j).getNoisePair().getRight();
          DRes<BigInteger> leftOpened = context.leftNumeric(par).open(leftClosed);
          DRes<BigInteger> rightOpened = context.rightNumeric(par).open(rightClosed);
          noisePairTestSet.add(new Pair<>(leftOpened, rightOpened));
          DRes<BigInteger> rhoToTest = context.rightNumeric(par).open(combinedPad.get(j).getRho());
          DRes<BigInteger> psiToTest = context.rightNumeric(par).open(combinedPad.get(j).getPsi());
          padTestSet.add(rhoToTest);
          padTestSet.add(psiToTest);
        }
      }
      Object[] data = new Object[] {
              noisePairsToKeep, noisePairTestSet, rhoToKeep, psiToKeep, padTestSet
      };
      return () -> data;
    }).par((par, data) -> { // Check selected pairs
      List<Pair<DRes<BigInteger>, DRes<BigInteger>>> noiseTestSet = (List<Pair<DRes<BigInteger>, DRes<BigInteger>>>) data[1];
      List<DRes<BigInteger>> padTestSet = (List<DRes<BigInteger>>) data[4];
      BigInteger modulo = BigInteger.valueOf(2).pow(securityParam);
      for (int i = 0; i < batchSize*(deterrenceFactor-1); i++) {
        BigInteger rp = noiseTestSet.get(i).getFirst().out();
        // TODO there was a bug here previously
        BigInteger rq = noiseTestSet.get(i).getSecond().out();
        if (rp.compareTo(rq) != 0) {
          throw new MaliciousException("Cheating in the noise pair");
        }
      }
      // TODO identify which party is malicious
      for (int i = 0; i < 2*batchSize*(deterrenceFactor-1); i++) {
        if (padTestSet.get(i).out().compareTo(modulo) >= 0) {
          throw new MaliciousException("Cheating in the size of psi or rho");
        }
      }
      List<CRTSInt> noisePairs = (List<CRTSInt>) data[0];
      List<SInt> rhoToKeep = (List<SInt>) data[2];
      List<SInt> psiToKeep = (List<SInt>) data[3];
      List<CRTCombinedPad> padsToUse = new ArrayList<>(batchSize);
      for (int i = 0; i < batchSize; i++) {
        padsToUse.add(new CRTCombinedPad(noisePairs.get(i), rhoToKeep.get(i), psiToKeep.get(i)));
      }
      return () -> padsToUse;
    });
  }
}