package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class CRTCovertDataSupplier<ResourcePoolL extends NumericResourcePool,
    ResourcePoolR extends NumericResourcePool>
        implements CRTDataSupplier {

  private final ArrayDeque<CRTSInt> noisePairs = new ArrayDeque<>();
  private final FieldDefinition fp;
  private final FieldDefinition fq;

  private final int partySize;
  private final int myId;
  private final int deterrenceFactor = 10;

  private class CovertNoiseGenerator extends CRTComputation<List<CRTSInt>, ResourcePoolL, ResourcePoolR> {

    private final int batchSize;
    private final SemiHonestNoiseGenerator<ResourcePoolL, ResourcePoolR> noiseGenerator;

    public CovertNoiseGenerator(int batchSize) {
      this.batchSize = batchSize;
      this.noiseGenerator = new SemiHonestNoiseGenerator<>(deterrenceFactor * batchSize);
    }

    @Override
    public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder,
                                                CRTNumericContext<ResourcePoolL, ResourcePoolR> context) {
      return builder.par(noiseGenerator::buildComputation).par((par, honestNoisePairs) -> {
        // Sample batchsize random noisepairs to keep, mark all other to be checked/selected
        Numeric numeric = par.numeric();
        List<DRes<BigInteger>> toKeep = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
          DRes<BigInteger> c = numeric.open(numeric.randomElement()); // TODO: check if this is smart
          toKeep.add(c);
        }
        return Pair.lazy(toKeep, honestNoisePairs);
      }).par((par, pair) -> { // Open selected pairs

        // prepare pairs indices to keep
        int[] toKeep = new int[batchSize];
        int i = 0;
        for (DRes<BigInteger> keep : pair.getFirst()) {
          long k = keep.out().longValue();
          // ensure we have c between 0 and gamma.
          toKeep[i] = (int) k % deterrenceFactor; // TODO: this might have to be done differently
        }
        List<CRTSInt> honestNoisePairs = pair.getSecond();

        // check all other pairs
        int j = 0;
        List<Pair<DRes<BigInteger>, DRes<BigInteger>>> testSet = new ArrayList<>();
        for (CRTSInt noisePair : honestNoisePairs) {
          i = j / batchSize; // current batch no.
          if (j == toKeep[i] + i*batchSize) // skip the once marked to keep.
            // scale them back up, so they correspond to a batch.
            continue;
          j++;
          DRes<SInt> leftClosed = noisePair.getLeft();
          DRes<SInt> rightClosed = noisePair.getRight();
          par.numeric().open(noisePair);
          DRes<BigInteger> leftOpened = context.leftNumeric(par).open(leftClosed);
          DRes<BigInteger> rightOpened = context.rightNumeric(par).open(rightClosed);
          testSet.add(new Pair<>(leftOpened, rightOpened));
        }

        // filter out the ones we need.
        List<CRTSInt> noisePairs = new ArrayList<>(batchSize);
        for (i = 0; i < batchSize; i++) {
          int k = toKeep[i] + i*batchSize;
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

  public CRTCovertDataSupplier(CRTResourcePool<ResourcePoolL,
      ResourcePoolR> resourcePool) {
    this.fp = resourcePool.getFieldDefinitions().getFirst();
    this.fq = resourcePool.getFieldDefinitions().getFirst();
    this.partySize = resourcePool.getNoOfParties();
    this.myId = resourcePool.getMyId();
  }

  @Override
  public DRes<CRTSInt> getCorrelatedNoise(ProtocolBuilderNumeric builder) {
    if (noisePairs.isEmpty()) {
      return builder.seq(new CovertNoiseGenerator(10)).seq((seq, noise) -> {
        noisePairs.addAll(noise);
        CRTSInt out = noisePairs.pop();
        return DRes.of(out);
      });
    } else {
      return DRes.of(noisePairs.pop());
    }
  }

  @Override
  public CRTSInt getRandomBit() {
    return null;
  }

  @Override
  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    return new Pair<>(fp, fq);
  }
}
