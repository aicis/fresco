package dk.alexandra.fresco.suite.marlin.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
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

  public MarlinMacCheckComputation(
      MarlinResourcePool<T> resourcePool) {
    this.resourcePool = resourcePool;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    Pair<List<MarlinSInt<T>>, List<T>> opened = resourcePool.getOpenedValueStore().popValues();
    List<MarlinSInt<T>> authenticatedElements = opened.getFirst();
    List<T> openValues = opened.getSecond();
    BigUIntFactory<T> factory = resourcePool.getFactory();
    T macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    // TODO use serializer
    List<byte[]> sharesLowBits = authenticatedElements.stream()
        .map(element -> ByteAndBitConverter.toByteArray(element.getShare().getLow()))
        .collect(Collectors.toList());
    final List<T> randomCoefficients = sampleRandomCoefficients(
        resourcePool.getRandomGenerator(),
        factory, openValues.size());
    final T y = BigUInt.innerProduct(openValues, randomCoefficients);
    final MarlinSInt<T> r = resourcePool.getDataSupplier().getNextRandomElementShare();
    ByteSerializer<T> serializer = resourcePool.getRawSerializer();
    return builder
        .seq(new MarlinBroadcastComputation<>(sharesLowBits))
        .seq((seq, ignored) -> {
          List<T> originalShares = authenticatedElements.stream()
              .map(MarlinSInt::getShare)
              .collect(Collectors.toList());
          List<T> overflow = originalShares.stream()
              .map(T::computeOverflow)
              .collect(Collectors.toList());
          List<T> randomCoefficientsLow = randomCoefficients.stream()
              .map(T::getLowAsUInt)
              .collect(Collectors.toList());
          T pj = BigUInt.innerProduct(overflow, randomCoefficientsLow);
          byte[] pjBytes = serializer.serialize(pj.add(r.getShare().getLowAsUInt()));
          return new MarlinBroadcastComputation<>(pjBytes).buildComputation(seq);
        })
        .seq((seq, broadcastPjs) -> {
          List<T> pjList = serializer.deserializeList(broadcastPjs);
          T pLow = BigUInt.sum(
              pjList.stream().map(BigUInt::getLowAsUInt).collect(Collectors.toList()));
          T p = factory.createFromLong(pLow.getLow());
          List<T> macShares = authenticatedElements.stream()
              .map(MarlinSInt::getMacShare)
              .collect(Collectors.toList());
          T mj = BigUInt.innerProduct(macShares, randomCoefficients);
          T zj = macKeyShare.multiply(y)
              .subtract(mj)
              .subtract(p.multiply(macKeyShare).shiftLowIntoHigh())
              .add(r.getMacShare().shiftLowIntoHigh());
          return new MarlinCommitmentComputation<>(resourcePool, serializer.serialize(zj))
              .buildComputation(seq);
        })
        .seq((seq, commitZjs) -> {
          if (!BigUInt.sum(serializer.deserializeList(commitZjs)).isZero()) {
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
      randomCoefficients.add(factory.createFromLong(drng.nextLong(Long.MAX_VALUE)));
    }
    return randomCoefficients;
  }

}
