package dk.alexandra.fresco.suite.spdz2k.protocols.computations;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntConverter;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.UInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDataSupplier;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kOpenedValueStore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Computation for performing batched mac-check on all currently opened, unchecked values.
 */
public class Spdz2kMacCheckComputation<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    PlainT extends CompUInt<HighT, LowT, PlainT>>
    implements Computation<Void, ProtocolBuilderNumeric> {

  private final CompUIntConverter<HighT, LowT, PlainT> converter;
  private final Spdz2kOpenedValueStore<PlainT> openedValueStore;
  private final ByteSerializer<PlainT> serializer;
  private final Spdz2kDataSupplier<PlainT> supplier;
  private List<PlainT> randomCoefficients;
  private ByteSerializer<HashBasedCommitment> commitmentSerializer;

  public Spdz2kMacCheckComputation(Spdz2kResourcePool<PlainT> resourcePool,
      CompUIntConverter<HighT, LowT, PlainT> converter) {
    this.openedValueStore = resourcePool.getOpenedValueStore();
    this.converter = converter;
    this.serializer = resourcePool.getPlainSerializer();
    this.supplier = resourcePool.getDataSupplier();
    this.randomCoefficients = sampleCoefficients(
        resourcePool.getRandomGenerator(),
        resourcePool.getFactory(), openedValueStore.size());
    this.commitmentSerializer = resourcePool.getCommitmentSerializer();
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    Pair<List<Spdz2kSInt<PlainT>>, List<PlainT>> opened = openedValueStore.peekValues();
    List<Spdz2kSInt<PlainT>> authenticatedElements = opened.getFirst();
    List<PlainT> openValues = opened.getSecond();
    PlainT macKeyShare = supplier.getSecretSharedKey();
    List<byte[]> sharesLowBits = authenticatedElements.stream()
        .map(element -> element.getShare().getLeastSignificant().toByteArray())
        .collect(Collectors.toList());
    PlainT y = UInt.innerProduct(openValues, randomCoefficients);
    Spdz2kSInt<PlainT> r = supplier.getNextRandomElementShare();
    return builder
        // note that we only care about the broadcast validation in this step; we ignore the actual
        // results of the broadcast since the parties already have them as authenticatedElements
        .seq(new BroadcastComputation<>(sharesLowBits))
        .seq((seq, ignored) -> computePValues(seq, authenticatedElements, r))
        .seq((seq, broadcastPjs) -> computeZValues(seq, authenticatedElements, macKeyShare, y, r,
            broadcastPjs))
        .seq((seq, commitZjs) -> {
          if (!UInt.sum(serializer.deserializeList(commitZjs)).isZero()) {
            throw new MaliciousException("Mac check failed");
          }
          openedValueStore.clear();
          return null;
        });
  }

  private DRes<List<byte[]>> computePValues(ProtocolBuilderNumeric builder,
      List<Spdz2kSInt<PlainT>> authenticatedElements,
      Spdz2kSInt<PlainT> r) {
    List<PlainT> originalShares = authenticatedElements.stream()
        .map(Spdz2kSInt::getShare)
        .collect(Collectors.toList());
    List<HighT> overflow = computeDifference(originalShares, converter);
    List<HighT> randomCoefficientsAsHigh = randomCoefficients.stream()
        .map(PlainT::getLeastSignificantAsHigh)
        .collect(Collectors.toList());
    HighT pj = UInt.innerProduct(overflow, randomCoefficientsAsHigh);
    byte[] pjBytes = pj.add(r.getShare().getLeastSignificantAsHigh()).toByteArray();
    return new BroadcastComputation<ProtocolBuilderNumeric>(pjBytes).buildComputation(builder);
  }

  private DRes<List<byte[]>> computeZValues(ProtocolBuilderNumeric builder,
      List<Spdz2kSInt<PlainT>> authenticatedElements,
      PlainT macKeyShare, PlainT y, Spdz2kSInt<PlainT> r,
      List<byte[]> broadcastPjs) {
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
    return new Spdz2kCommitmentComputation(commitmentSerializer, serializer.serialize(zj))
        .buildComputation(builder);
  }

  /**
   * Samples random coefficients for mac-check using joint source of randomness.
   */
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

  /**
   * For each share v, where low denotes the value representing the k lower bits of v, and s is the
   * number of upper bits, compute ((low - s) % 2^{k + s} >> k) % 2^s.
   */
  private List<HighT> computeDifference(List<PlainT> originalShares,
      CompUIntConverter<HighT, LowT, PlainT> converter) {
    return originalShares.stream()
        .map(value -> {
          PlainT low = converter.createFromLow(value.getLeastSignificant());
          return low.subtract(value).getMostSignificant();
        })
        .collect(Collectors.toList());
  }

}
