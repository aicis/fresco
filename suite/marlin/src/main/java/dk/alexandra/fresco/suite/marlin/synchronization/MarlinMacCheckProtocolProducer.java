package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MarlinMacCheckProtocolProducer<T extends BigUInt<T>> implements ProtocolProducer {

  private final SequentialProtocolProducer protocolProducer;
  private MarlinBroadcastProtocolProducer<T> broadcastPjs;
  private MarlinCommitmentProtocolProducer<T> commitZjs;
  private List<T> randomCoefficients;
  private T y;
  private MarlinSInt<T> r;

  public MarlinMacCheckProtocolProducer(MarlinResourcePool<T> resourcePool) {
    final Pair<List<MarlinSInt<T>>, List<T>> opened = resourcePool.getOpenedValueStore()
        .popValues();
    final List<MarlinSInt<T>> authenticatedElements = opened.getFirst();
    final List<T> openValues = opened.getSecond();
    final BigUIntFactory<T> factory = resourcePool.getFactory();
    final T macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    protocolProducer = new SequentialProtocolProducer();
//    protocolProducer = new SequentialProtocolProducer(
//        new SequentialProtocolProducer(new SingleProtocolProducer<>(
//            // TODO make sure that running broadcast validation retro-actively is okay
//            // TODO only run broadcast on lower bits
//            new MarlinBroadcastValidationProtocol<>(
//                sharesAndMacs.stream().map(MarlinSInt::getShare).collect(
//                    Collectors.toList())))));
    protocolProducer.lazyAppend(() -> {
      randomCoefficients = sampleRandomCoefficients(resourcePool.getRandomGenerator(),
          factory, openValues.size());
      y = BigUInt.innerProduct(openValues, randomCoefficients);
      r = resourcePool.getDataSupplier().getNextRandomElementShare();
      List<T> originalShares = authenticatedElements.stream()
          .map(MarlinSInt::getShare)
          .collect(Collectors.toList());
      long pj = computeLinearCombination(
          computeOverflow(originalShares, factory), randomCoefficients);
      broadcastPjs = new MarlinBroadcastProtocolProducer<>(
          ByteAndBitConverter.toByteArray(pj + r.getShare().getLow()));
      return broadcastPjs;
    });
    protocolProducer.lazyAppend(() -> {
      List<T> pjList = resourcePool.getRawSerializer().deserializeList(broadcastPjs.out());
      T p = factory.createFromLong(sum(getLowsAsLongs(pjList)));
      List<T> macShares = authenticatedElements.stream()
          .map(MarlinSInt::getMacShare)
          .collect(Collectors.toList());
      T mj = BigUInt.innerProduct(macShares, randomCoefficients);
      T zj = macKeyShare.multiply(y)
          .subtract(mj)
          .subtract(p.multiply(macKeyShare).shiftLowIntoHigh())
          .add(r.getMacShare().shiftLowIntoHigh());
      commitZjs = new MarlinCommitmentProtocolProducer<>(resourcePool, zj);
      return commitZjs;
    });
    protocolProducer.lazyAppend(() -> {
      if (!BigUInt.sum(commitZjs.getResult()).isZero()) {
        throw new MaliciousException("Mac check failed");
      }
      return new SequentialProtocolProducer();
    });
  }

  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    protocolProducer.getNextProtocols(protocolCollection);
  }

  @Override
  public boolean hasNextProtocols() {
    return protocolProducer.hasNextProtocols();
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
