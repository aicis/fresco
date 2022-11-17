package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CRTSemiHonestDataSupplier<ResourcePoolL extends NumericResourcePool,
    ResourcePoolR extends NumericResourcePool>
        implements CRTDataSupplier {

  private final int myId;
  private final int players;
  private final CRTResourcePool<ResourcePoolL, ResourcePoolR> resourcePool;
  private final Random random;
  private ArrayDeque<CRTSInt> noisePairs = new ArrayDeque<>();
  private final FieldDefinition leftDef;
  private final FieldDefinition rightDef;

  private class NoiseGenerator implements ComputationParallel<ArrayDeque<CRTSInt>, ProtocolBuilderNumeric> {

    private final int batchSize;

    public NoiseGenerator(int batchSize) {
      this.batchSize = batchSize;
    }

    @Override
    public DRes<ArrayDeque<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.par(par -> {
        Numeric numeric = par.numeric();
        List<CRTSInt> list = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
          // TODO: Actually do the protocol
          BigInteger r = Util.randomBigInteger(random, resourcePool.getFieldDefinitions().getFirst().getModulus());
          BigInteger l = Util.randomBigInteger(random, BigInteger.valueOf(players));
          CRTSInt noisePair = new CRTSInt(
                  numeric.known(leftDef.createElement(r).toBigInteger()),
                  numeric.known(rightDef.createElement(r.add(l.multiply(leftDef.getModulus()))).toBigInteger())
          );
          list.add(noisePair);
        }
        return DRes.of(list);
      }).seq((seq, res) -> {
        ArrayDeque resDeque = new ArrayDeque<>();
        resDeque.addAll(res);
        return ()-> resDeque;
      });
    }
  }

  public CRTSemiHonestDataSupplier(int myId, int players, CRTResourcePool<ResourcePoolL,
      ResourcePoolR> resourcePool) {
    this.myId = myId;
    this.players = players;
    this.resourcePool = resourcePool;

    this.random = new SecureRandom();
    this.leftDef = resourcePool.getFieldDefinitions().getFirst();
    this.rightDef = resourcePool.getFieldDefinitions().getFirst();

  }

  @Override
  public CRTSInt getCorrelatedNoise(ProtocolBuilderNumeric builder) {
    if (!noisePairs.isEmpty()) {
      return noisePairs.pop();
    }
    noisePairs = builder.seq(new NoiseGenerator(10)).out();
    return noisePairs.pop();
  }

  @Override
  public CRTSInt getRandomBit() {
    return null;
  }

  @Override
  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    return new Pair<>(leftDef, rightDef);
  }
}
