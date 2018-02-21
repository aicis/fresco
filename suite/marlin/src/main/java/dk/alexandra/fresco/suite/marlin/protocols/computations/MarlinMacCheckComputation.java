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

public class MarlinMacCheckComputation<
    HighT extends UInt<HighT>,
    LowT extends UInt<LowT>,
    CompT extends CompUInt<HighT, LowT, CompT>>
    implements Computation<Void, ProtocolBuilderNumeric> {

  private final MarlinResourcePool<HighT, LowT, CompT> resourcePool;

  public MarlinMacCheckComputation(
      MarlinResourcePool<HighT, LowT, CompT> resourcePool) {
    this.resourcePool = resourcePool;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    Pair<List<MarlinSInt<HighT, LowT, CompT>>, List<CompT>> opened = resourcePool
        .getOpenedValueStore()
        .popValues();
    List<MarlinSInt<HighT, LowT, CompT>> authenticatedElements = opened.getFirst();
    System.out.println("authenticatedElements " + authenticatedElements);
    List<CompT> openValues = opened.getSecond();
    System.out.println("openValues " + openValues);
    ByteSerializer<CompT> serializer = resourcePool.getRawSerializer();
    CompUIntFactory<HighT, LowT, CompT> factory = resourcePool.getFactory();
    CompT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    List<byte[]> sharesLowBits = authenticatedElements.stream()
        .map(element -> element.getShare().getLeastSignificant().toByteArray())
        .collect(Collectors.toList());
    System.out.println("macKeyShare " + macKeyShare);
    final List<CompT> randomCoefficients = sampleCoefficients(
        resourcePool.getRandomGenerator(),
        factory, openValues.size());
    System.out.println("randomCoefficients " + randomCoefficients);
    final CompT y = UInt.innerProduct(openValues, randomCoefficients);
    System.out.println("y " + y);
    final MarlinSInt<HighT, LowT, CompT> r = resourcePool.getDataSupplier()
        .getNextRandomElementShare();
    System.out.println("r " + r);
    return builder
        .seq(new MarlinBroadcastComputation(sharesLowBits))
        .seq((seq, ignored) -> {
          List<CompT> originalShares = authenticatedElements.stream()
              .map(MarlinSInt::getShare)
              .collect(Collectors.toList());
          System.out.println("originalShares " + originalShares);
          List<HighT> overflow = originalShares.stream()
              .map(CompT::computeOverflow)
              .collect(Collectors.toList());
          System.out.println("overflow " + overflow);
          List<HighT> randomCoefficientsAsHigh = randomCoefficients.stream()
              .map(CompT::getLeastSignificantAsHigh)
              .collect(Collectors.toList());
          System.out.println("randomCoefficientsAsHigh " + randomCoefficientsAsHigh);
          HighT pj = UInt.innerProduct(overflow, randomCoefficientsAsHigh);
//          System.out.println("pj " + pj);
          System.out.println("pj + r " + pj.add(r.getShare().getLeastSignificantAsHigh()));
          byte[] pjBytes = pj.add(r.getShare().getLeastSignificantAsHigh()).toByteArray();
          return new MarlinBroadcastComputation(pjBytes).buildComputation(seq);
        })
        .seq((seq, broadcastPjs) -> {
          List<CompT> pjList = serializer.deserializeList(broadcastPjs);
          System.out.println("pjList " + pjList);
          HighT pLow = UInt.sum(
              pjList.stream().map(CompT::getLeastSignificantAsHigh).collect(Collectors.toList()));
          System.out.println("pLow " + pLow);
          CompT p = factory.createFromHigh(pLow);
          System.out.println("p " + p);
          List<CompT> macShares = authenticatedElements.stream()
              .map(MarlinSInt::getMacShare)
              .collect(Collectors.toList());
          CompT mj = UInt.innerProduct(macShares, randomCoefficients);
          System.out.println("mj " + mj);
          CompT zj = macKeyShare.multiply(y)
              .subtract(mj)
              .subtract(p.multiply(macKeyShare).shiftLowIntoHigh())
              .add(r.getMacShare().shiftLowIntoHigh());
          System.out.println("zj " + zj);
          return new MarlinCommitmentComputation(resourcePool.getCommitmentSerializer(),
              serializer.serialize(zj))
              .buildComputation(seq);
        })
        .seq((seq, commitZjs) -> {
          System.out.println("commitZjs " + serializer.deserializeList(commitZjs));
          if (!UInt.sum(serializer.deserializeList(commitZjs)).isZero()) {
            throw new MaliciousException("Mac check failed");
          }
          return null;
        });
  }

  private List<CompT> sampleCoefficients(Drbg drbg, CompUIntFactory<HighT, LowT, CompT> factory,
      int numCoefficients) {
    List<CompT> randomCoefficients = new ArrayList<>(numCoefficients);
    for (int i = 0; i < numCoefficients; i++) {
      byte[] bytes = new byte[factory.getHighBitLength() / Byte.SIZE];
      drbg.nextBytes(bytes);
      randomCoefficients.add(factory.createFromBytes(bytes));
    }
    return randomCoefficients;
  }

}
