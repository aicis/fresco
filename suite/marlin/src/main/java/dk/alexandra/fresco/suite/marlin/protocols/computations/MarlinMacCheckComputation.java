package dk.alexandra.fresco.suite.marlin.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.MarlinSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.MarlinResourcePool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MarlinMacCheckComputation<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> implements
    Computation<Void, ProtocolBuilderNumeric> {

  private final MarlinResourcePool<H, L, T> resourcePool;

  public MarlinMacCheckComputation(
      MarlinResourcePool<H, L, T> resourcePool) {
    this.resourcePool = resourcePool;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    Pair<List<MarlinSInt<H, L, T>>, List<T>> opened = resourcePool.getOpenedValueStore().popValues();
    List<MarlinSInt<H, L, T>> authenticatedElements = opened.getFirst();
    List<T> openValues = opened.getSecond();
    ByteSerializer<T> serializer = resourcePool.getRawSerializer();
    CompUIntFactory<H, L, T> factory = resourcePool.getFactory();
    T macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    // TODO figure out serializers
    List<byte[]> sharesLowBits = authenticatedElements.stream()
        .map(element -> element.getShare().getLow().toByteArray())
        .collect(Collectors.toList());
    final List<T> randomCoefficients = sampleCoefficients(
        resourcePool.getRandomGenerator(),
        factory, openValues.size());
    final T y = UInt.innerProduct(openValues, randomCoefficients);
    final MarlinSInt<H, L, T> r = resourcePool.getDataSupplier().getNextRandomElementShare();
    return builder
        .seq(new MarlinBroadcastComputation<>(sharesLowBits))
        .seq((seq, ignored) -> {
          List<T> originalShares = authenticatedElements.stream()
              .map(MarlinSInt::getShare)
              .collect(Collectors.toList());
          List<L> overflow = originalShares.stream()
              .map(T::computeOverflow)
              .collect(Collectors.toList());
          List<L> randomCoefficientsLow = randomCoefficients.stream()
              .map(T::getLow)
              .collect(Collectors.toList());
          L pj = UInt.innerProduct(overflow, randomCoefficientsLow);
          byte[] pjBytes = pj.add(r.getShare().getLow()).toByteArray();
          return new MarlinBroadcastComputation<>(pjBytes).buildComputation(seq);
        })
        .seq((seq, broadcastPjs) -> {
          List<T> pjList = serializer.deserializeList(broadcastPjs);
          L pLow = UInt.sum(
              pjList.stream().map(T::getLow).collect(Collectors.toList()));
          T p = factory.createFromLow(pLow);
          List<T> macShares = authenticatedElements.stream()
              .map(MarlinSInt::getMacShare)
              .collect(Collectors.toList());
          T mj = UInt.innerProduct(macShares, randomCoefficients);
          T zj = macKeyShare.multiply(y)
              .subtract(mj)
              .subtract(p.multiply(macKeyShare).shiftLowIntoHigh())
              .add(r.getMacShare().shiftLowIntoHigh());
          return new MarlinCommitmentComputation<>(resourcePool, serializer.serialize(zj))
              .buildComputation(seq);
        })
        .seq((seq, commitZjs) -> {
          if (!UInt.sum(serializer.deserializeList(commitZjs)).isZero()) {
            throw new MaliciousException("Mac check failed");
          }
          return null;
        });
  }

  private List<T> sampleCoefficients(Drbg drbg, CompUIntFactory<H, L, T> factory, int numCoefficients) {
    List<T> randomCoefficients = new ArrayList<>(numCoefficients);
    for (int i = 0; i < numCoefficients; i++) {
      byte[] bytes = new byte[factory.getHighBitLength() / Byte.SIZE];
      drbg.nextBytes(bytes);
      randomCoefficients.add(factory.createFromBytes(bytes));
    }
    return randomCoefficients;
  }

}
