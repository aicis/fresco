package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class CRTSemiHonestDataSupplier<ResourcePoolL extends NumericResourcePool,
    ResourcePoolR extends NumericResourcePool>
        implements CRTDataSupplier {

  private final int players;
  private final SecureRandom random;
  private final ArrayDeque<CRTSInt> noisePairs = new ArrayDeque<>();
  private final FieldDefinition fp;
  private final FieldDefinition fq;

  private class NoiseGenerator extends CRTComputation<List<CRTSInt>, ResourcePoolL, ResourcePoolR> {

    private final int batchSize;

    public NoiseGenerator(int batchSize) {
      this.batchSize = batchSize;
    }

    @Override
    public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder, CRTNumericContext<ResourcePoolL, ResourcePoolR> context) {
      return builder.par(par -> {
        Numeric left = context.leftNumeric(par);
        Numeric right = context.rightNumeric(par);
        List<CRTSInt> list = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
          DRes<SInt> rp = left.randomElement();
          // not really sure how `l` (delta) should be selected.
          BigInteger l = Util.randomBigInteger(random, BigInteger.valueOf(players));
          DRes<SInt> rq = right.add(rp, left.known(l.multiply(fp.getModulus())));
          CRTSInt noisePair = new CRTSInt(rp, rq);
          list.add(noisePair);
        }
        return DRes.of(list);
      });
    }

  }

  public CRTSemiHonestDataSupplier(int players, CRTResourcePool<ResourcePoolL,
      ResourcePoolR> resourcePool) {
    this.players = players;
    this.random = new SecureRandom();
    this.fp = resourcePool.getFieldDefinitions().getFirst();
    this.fq = resourcePool.getFieldDefinitions().getFirst();
  }

  @Override
  public DRes<CRTSInt> getCorrelatedNoise(ProtocolBuilderNumeric builder) {
    if (noisePairs.isEmpty()) {
      return builder.seq(new NoiseGenerator(10)).seq((seq, noise) -> {
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
