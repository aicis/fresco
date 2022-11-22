package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.protocols.framework.CRTComputation;

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

  private class NoiseGenerator extends CRTComputation<List<CRTSInt>, ResourcePoolL, ResourcePoolR> {

    private final int batchSize;

    public NoiseGenerator(int batchSize) {
      this.batchSize = batchSize;
    }

    @Override
    public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder, CRTNumericContext<ResourcePoolL, ResourcePoolR> context) {
      return builder.par(par -> {
        Numeric numeric = par.numeric();
        List<CRTSInt> list = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
          // TODO: Actually do the protocol
          BigInteger r = Util.randomBigInteger(random, resourcePool.getFieldDefinitions().getFirst().getModulus());
          BigInteger l = Util.randomBigInteger(random, BigInteger.valueOf(players));
          CRTSInt noisePair = new CRTSInt(
                  context.leftNumeric(par).known(leftDef.createElement(r).toBigInteger()),
                  context.rightNumeric(par).known(rightDef.createElement(r.add(l.multiply(leftDef.getModulus()))).toBigInteger())
          );
          list.add(noisePair);
        }
        return () ->  list;
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
    return new Pair<>(leftDef, rightDef);
  }
}
