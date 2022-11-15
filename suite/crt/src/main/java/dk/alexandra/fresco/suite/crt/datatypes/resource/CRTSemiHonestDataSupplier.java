package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.CRTProtocolSuite;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CRTSemiHonestDataSupplier<A extends NumericResourcePool, B extends NumericResourcePool>
        implements CRTDataSupplier {

  private final int myId;
  private final int players;
  private final CRTProtocolSuite<A,B> suite;
  private final Random random;
  private final ArrayDeque<CRTSInt> noisePairs;
  private final FieldDefinition fp;
  private final FieldDefinition fq;

  private class NoiseGenerator implements ComputationParallel<List<CRTSInt>, ProtocolBuilderNumeric> {

    private final int batchSize;

    public NoiseGenerator(int batchSize) {
      this.batchSize = batchSize;
    }

    @Override
    public DRes<List<CRTSInt>> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.par(par -> {
        Numeric numeric = par.numeric();
        List<CRTSInt> list = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
          // TODO: Actually do the protocol
          BigInteger r = Util.randomBigInteger(random, suite.getLeft().getBasicNumericContext().getModulus());
          BigInteger l = Util.randomBigInteger(random, BigInteger.valueOf(players));
          CRTSInt noisePair = new CRTSInt(
                  numeric.known(fp.createElement(r).toBigInteger()),
                  numeric.known(fq.createElement(r.add(l.multiply(fp.getModulus()))).toBigInteger())
          );
          list.add(noisePair);
        }
        return DRes.of(list);
      });
    }
  }

  public CRTSemiHonestDataSupplier(int myId, int players, CRTProtocolSuite<A,B> suite) {
    this.myId = myId;
    this.players = players;
    this.suite = suite;

    this.random = new SecureRandom();
    this.fp = suite.getLeft().getBasicNumericContext().getFieldDefinition();
    this.fq = suite.getLeft().getBasicNumericContext().getFieldDefinition();

    this.noisePairs = new ArrayDeque<>();

    Application<?, ProtocolBuilderNumeric> app = builder -> {
      DRes<List<CRTSInt>> res = builder.seq(new NoiseGenerator(10));
      return builder.seq(seq -> {
        noisePairs.addAll(res.out());
        return null;
      });
    };
    suite.getLeft().createSequential().seq(app);
  }

  @Override
  public CRTSInt getCorrelatedNoise() {
    return noisePairs.pop();
  }

  @Override
  public CRTSInt getRandomBit() {
    return new CRTSInt(
            suite.getLeft().createSequential().numeric().randomBit(),
            suite.getRight().createSequential().numeric().randomBit()
    );
  }

  @Override
  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    return new Pair<>(fp, fq);
  }
}
