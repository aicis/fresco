package dk.alexandra.fresco.suite.marlin.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.marlin.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.marlin.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Spdz2kMultiplyProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<SInt, PlainT> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private Spdz2kTriple<PlainT> triple;
  private SInt product;
  private Spdz2kSInt<PlainT> epsilon;
  private Spdz2kSInt<PlainT> delta;

  public Spdz2kMultiplyProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    final PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    ByteSerializer<PlainT> serializer = resourcePool.getRawSerializer();
    if (round == 0) {
      triple = resourcePool.getDataSupplier().getNextTripleShares();
      epsilon = ((Spdz2kSInt<PlainT>) left.out()).subtract(triple.getLeft());
      delta = ((Spdz2kSInt<PlainT>) right.out()).subtract(triple.getRight());
      network.sendToAll(epsilon.getShare().getLeastSignificant().toByteArray());
      network.sendToAll(delta.getShare().getLeastSignificant().toByteArray());
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      Pair<PlainT, PlainT> epsilonAndDelta = receiveAndReconstruct(network,
          resourcePool.getNoOfParties(),
          serializer);
      // compute [prod] = [c] + epsilon * [b] + delta * [a] + epsilon * delta
      PlainT e = epsilonAndDelta.getFirst();
      PlainT d = epsilonAndDelta.getSecond();
      PlainT ed = e.multiply(d);
      product = triple.getProduct()
          .add(triple.getRight().multiply(e))
          .add(triple.getLeft().multiply(d))
          .addConstant(ed, resourcePool.getMyId(), macKeyShare, resourcePool.getFactory().zero());
      resourcePool.getOpenedValueStore().pushOpenedValues(
          Arrays.asList(epsilon, delta),
          Arrays.asList(e, d)
      );
      // TODO is this really necessary?
      triple = null;
      epsilon = null;
      delta = null;
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilon and delta and reconstructs each.
   */
  private Pair<PlainT, PlainT> receiveAndReconstruct(Network network, int noOfParties,
      ByteSerializer<PlainT> serializer) {
    List<PlainT> epsilonShares = new ArrayList<>(noOfParties);
    List<PlainT> deltaShares = new ArrayList<>(noOfParties);
    for (int i = 1; i <= noOfParties; i++) {
      epsilonShares.add(serializer.deserialize(network.receive(i)));
      deltaShares.add(serializer.deserialize(network.receive(i)));
    }
    PlainT e = UInt.sum(epsilonShares);
    PlainT d = UInt.sum(deltaShares);
    return new Pair<>(e, d);
  }

  @Override
  public SInt out() {
    return product;
  }

}
