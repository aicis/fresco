package dk.alexandra.fresco.suite.marlin.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntConverter;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Spdz2kMacCheckComputation<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    PlainT extends CompUInt<HighT, LowT, PlainT>>
    implements Computation<Void, ProtocolBuilderNumeric> {

  private final Spdz2kResourcePool<PlainT> resourcePool;
  private final CompUIntConverter<HighT, LowT, PlainT> converter;

  public Spdz2kMacCheckComputation(Spdz2kResourcePool<PlainT> resourcePool,
      CompUIntConverter<HighT, LowT, PlainT> converter) {
    this.resourcePool = resourcePool;
    this.converter = converter;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    Pair<List<Spdz2kSInt<PlainT>>, List<PlainT>> opened = resourcePool
        .getOpenedValueStore()
        .popValues();
    List<Spdz2kSInt<PlainT>> authenticatedElements = opened.getFirst();
    List<PlainT> openValues = opened.getSecond();
    ByteSerializer<PlainT> serializer = resourcePool.getRawSerializer();
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    List<byte[]> sharesLowBits = authenticatedElements.stream()
        .map(element -> element.getShare().getLeastSignificant().toByteArray())
        .collect(Collectors.toList());
    final List<PlainT> randomCoefficients = sampleCoefficients(
        resourcePool.getRandomGenerator(),
        factory, openValues.size());
    final PlainT y = UInt.innerProduct(openValues, randomCoefficients);
    final Spdz2kSInt<PlainT> r = resourcePool.getDataSupplier()
        .getNextRandomElementShare();
    return builder
        .seq(new BroadcastComputation<>(sharesLowBits))
        .seq((seq, ignored) -> {
          List<PlainT> originalShares = authenticatedElements.stream()
              .map(Spdz2kSInt::getShare)
              .collect(Collectors.toList());
          List<HighT> overflow = originalShares.stream()
              .map(PlainT::computeOverflow)
              .collect(Collectors.toList());
          List<HighT> randomCoefficientsAsHigh = randomCoefficients.stream()
              .map(PlainT::getLeastSignificantAsHigh)
              .collect(Collectors.toList());
          HighT pj = UInt.innerProduct(overflow, randomCoefficientsAsHigh);
          byte[] pjBytes = pj.add(r.getShare().getLeastSignificantAsHigh()).toByteArray();
          return new BroadcastComputation<ProtocolBuilderNumeric>(pjBytes).buildComputation(seq);
        })
        .seq((seq, broadcastPjs) -> {
          List<PlainT> pjList = serializer.deserializeList(broadcastPjs);
          HighT pLow = UInt.sum(
              pjList.stream().map(PlainT::getLeastSignificantAsHigh).collect(Collectors.toList()));
          PlainT p = converter.createFromHigh(pLow);
          List<PlainT> macShares = authenticatedElements.stream()
              .map(Spdz2kSInt::getMacShare)
              .collect(Collectors.toList());
          PlainT mj = UInt.innerProduct(macShares, randomCoefficients);
          PlainT zj = macKeyShare.multiply(y)
              .subtract(mj)
              .subtract(p.multiply(macKeyShare).shiftLowIntoHigh())
              .add(r.getMacShare().shiftLowIntoHigh());
          return new Spdz2kCommitmentComputation(resourcePool.getCommitmentSerializer(),
              serializer.serialize(zj))
              .buildComputation(seq);
        })
        .seq((seq, commitZjs) -> {
          if (!UInt.sum(serializer.deserializeList(commitZjs)).isZero()) {
            throw new MaliciousException("Mac check failed");
          }
          return null;
        });
  }

  private List<PlainT> sampleCoefficients(Drbg drbg, CompUIntFactory<PlainT> factory,
      int numCoefficients) {
    List<PlainT> randomCoefficients = new ArrayList<>(numCoefficients);
    for (int i = 0; i < numCoefficients; i++) {
      byte[] bytes = new byte[factory.getHighBitLength() / Byte.SIZE];
      drbg.nextBytes(bytes);
      randomCoefficients.add(factory.createFromBytes(bytes));
    }
    return randomCoefficients;
  }

}
