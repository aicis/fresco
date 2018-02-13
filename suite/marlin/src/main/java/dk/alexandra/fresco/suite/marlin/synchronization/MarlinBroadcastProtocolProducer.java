package dk.alexandra.fresco.suite.marlin.synchronization;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.protocols.MarlinBroadcastValidationProtocol;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MarlinMacCheckProtocolProducer<T extends BigUInt<T>> implements ProtocolProducer {

  private final SequentialProtocolProducer protocolProducer;
  private MarlinBroadcastValidationProtocol<T> broadcastMasked;
  private List<T> randomCoefficients;
  private T linearCombination;

  public MarlinMacCheckProtocolProducer(MarlinResourcePool<T> resourcePool) {
    final Pair<List<MarlinSInt<T>>, List<T>> opened = resourcePool.getOpenedValueStore()
        .popValues();
    final List<MarlinSInt<T>> sharesAndMacs = opened.getFirst();
    final List<T> openValues = opened.getSecond();
    final BigUIntFactory<T> factory = resourcePool.getFactory();
    protocolProducer = new SequentialProtocolProducer(
        // TODO make sure that running broadcast validation retro-actively is okay
        // TODO only run broadcast on lower bits
        new SingleProtocolProducer<>(
            new MarlinBroadcastValidationProtocol<>(sharesAndMacs.stream().map(MarlinSInt::getShare).collect(
                Collectors.toList())))
    );
    protocolProducer.append(
        new LazyProtocolProducerDecorator(() -> {
          randomCoefficients = sampleRandomCoefficients(resourcePool.getRandomGenerator(),
              factory, openValues.size());
          linearCombination = BigUInt.innerProduct(openValues, randomCoefficients);
          MarlinSInt<T> randomShare = resourcePool.getDataSupplier().getNextRandomElementShare();
          List<T> originalShares = sharesAndMacs.stream()
              .map(MarlinSInt::getShare)
              .collect(Collectors.toList());
          long[] overflow = computeOverflow(originalShares, factory);
          long linearCombinationOverflow = computeLinearCombination(overflow, randomCoefficients);
          long masked = linearCombinationOverflow + randomShare.getShare().getLow();
          System.out.println("local masked " + masked);
          broadcastMasked = new MarlinBroadcastValidationProtocol<>(
              factory.createFromLong(masked));
          return new SingleProtocolProducer<>(broadcastMasked);
        }));
    protocolProducer.append(
        new LazyProtocolProducerDecorator(() -> {
          List<T> maskedOverflow = broadcastMasked.out();
          System.out.println("local masked " + maskedOverflow);
          return new SequentialProtocolProducer();
        }));
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
      // TODO check that the upper bound is correct
      randomCoefficients.add(factory.createFromLong(drng.nextLong(Long.MAX_VALUE)));
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

}
