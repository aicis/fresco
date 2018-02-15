package dk.alexandra.fresco.suite.marlin.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MarlinMacCheckComputation<T extends BigUInt<T>> implements
    Computation<Void, ProtocolBuilderNumeric> {

  private final MarlinResourcePool<T> resourcePool;
  private List<T> randomCoefficients;
  private T y;
  private MarlinSInt<T> r;

  public MarlinMacCheckComputation(
      MarlinResourcePool<T> resourcePool) {
    this.resourcePool = resourcePool;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    Pair<List<MarlinSInt<T>>, List<T>> opened = resourcePool.getOpenedValueStore()
        .popValues();
    List<MarlinSInt<T>> authenticatedElements = opened.getFirst();
    List<T> openValues = opened.getSecond();
    BigUIntFactory<T> factory = resourcePool.getFactory();
    T macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    List<byte[]> sharesLowBits = authenticatedElements.stream()
        .map(element -> ByteAndBitConverter.toByteArray(element.getShare().getLow()))
        .collect(Collectors.toList());
    return builder
        .seq(new MarlinBroadcastComputation<>(sharesLowBits))
        .seq((seq, ignored) -> {
          randomCoefficients = sampleRandomCoefficients(resourcePool.getRandomGenerator(),
              factory, openValues.size());
          y = BigUInt.innerProduct(openValues, randomCoefficients);
          r = resourcePool.getDataSupplier().getNextRandomElementShare();
          List<T> originalShares = authenticatedElements.stream()
              .map(MarlinSInt::getShare)
              .collect(Collectors.toList());
          long pj = computeLinearCombination(
              computeOverflow(originalShares, factory), randomCoefficients);
          byte[] pjBytes = ByteAndBitConverter.toByteArray(pj + r.getShare().getLow());
          return new MarlinBroadcastComputation<>(pjBytes).buildComputation(seq);
        })
        .seq((seq, broadcastPjs) -> {
          List<T> pjList = resourcePool.getRawSerializer().deserializeList(broadcastPjs);
          T p = factory.createFromLong(sum(getLowsAsLongs(pjList)));
          List<T> macShares = authenticatedElements.stream()
              .map(MarlinSInt::getMacShare)
              .collect(Collectors.toList());
          T mj = BigUInt.innerProduct(macShares, randomCoefficients);
          T zj = macKeyShare.multiply(y)
              .subtract(mj)
              .subtract(p.multiply(macKeyShare).shiftLowIntoHigh())
              .add(r.getMacShare().shiftLowIntoHigh());
          byte[] zjBytes = resourcePool.getRawSerializer().serialize(zj);
          return new MarlinCommitmentComputation<>(resourcePool, zjBytes).buildComputation(seq);
        })
        .seq((seq, commitZjs) -> {
          List<T> deserialized = resourcePool.getRawSerializer().deserializeList(commitZjs);
          if (!BigUInt.sum(deserialized).isZero()) {
            throw new MaliciousException("Mac check failed");
          }
          return null;
        });
  }

  private List<T> sampleRandomCoefficients(Drbg drbg, BigUIntFactory<T> factory,
      int numCoefficients) {
    List<T> randomCoefficients = new ArrayList<>(numCoefficients);
    Drng drng = new DrngImpl(drbg);
    for (int i = 0; i < numCoefficients; i++) {
      // TODO check upper bound
      randomCoefficients.add(
          factory.createFromLong(drng.nextLong(Long.MAX_VALUE)));
    }
    return randomCoefficients;
  }

  private long[] computeOverflow(List<T> shares, BigUIntFactory<T> factory) {
    long[] overflow = new long[shares.size()];
    for (int i = 0; i < overflow.length; i++) {
      T share = shares.get(i);
      T lower = factory.createFromLong(share.getLow());
      T diff = lower.subtract(share);
      overflow[i] = diff.getHigh();
    }
    return overflow;
  }

  private long computeLinearCombination(long[] overflow, List<T> randomCoefficients) {
    long sum = 0;
    for (int i = 0; i < overflow.length; i++) {
      sum += overflow[i] * randomCoefficients.get(i).getLow();
    }
    return sum;
  }

  private long sum(long[] elements) {
    long sum = 0;
    for (long element : elements) {
      sum += element;
    }
    return sum;
  }

  private long[] getLowsAsLongs(List<T> elements) {
    long[] lows = new long[elements.size()];
    for (int i = 0; i < lows.length; i++) {
      lows[i] = elements.get(i).getLow();
    }
    return lows;
  }

}
