package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.datatypes.UInt;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.List;

public class Spdz2kBatchMultiplyProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<List<DRes<SInt>>, PlainT> {

  private final DRes<List<DRes<SInt>>> leftFactors;
  private final DRes<List<DRes<SInt>>> rightFactors;
  private List<Spdz2kTriple<PlainT>> triples;
  private List<DRes<SInt>> products;
  private List<Spdz2kSInt<PlainT>> epsilons;
  private List<Spdz2kSInt<PlainT>> deltas;

  public Spdz2kBatchMultiplyProtocol(DRes<List<DRes<SInt>>> leftFactors,
      DRes<List<DRes<SInt>>> rightFactors) {
    this.leftFactors = leftFactors;
    this.rightFactors = rightFactors;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    ByteSerializer<PlainT> serializer = resourcePool.getPlainSerializer();
    if (round == 0) {
      List<DRes<SInt>> leftOut = leftFactors.out();
      List<DRes<SInt>> rightOut = rightFactors.out();
      this.epsilons = new ArrayList<>(leftOut.size());
      this.deltas = new ArrayList<>(rightOut.size());
      triples = resourcePool.getDataSupplier().getNextTripleShares(leftOut.size());
      for (int i = 0; i < leftOut.size(); i++) {
        Spdz2kSInt<PlainT> left = toSpdz2kSInt(leftOut.get(i));
        Spdz2kSInt<PlainT> epsilon = left.subtract(triples.get(i).getLeft());
        epsilons.add(epsilon);
        Spdz2kSInt<PlainT> right = toSpdz2kSInt(rightOut.get(i));
        Spdz2kSInt<PlainT> delta = right.subtract(triples.get(i).getRight());
        deltas.add(delta);
      }
      for (int i = 0; i < leftOut.size(); i++) {
        network.sendToAll(epsilons.get(i).getShare().getLeastSignificant().toByteArray());
        network.sendToAll(deltas.get(i).getShare().getLeastSignificant().toByteArray());
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      products = new ArrayList<>(epsilons.size());
      List<Pair<PlainT, PlainT>> epsilonAndDelta = receiveAndReconstruct(network,
          resourcePool.getNoOfParties(),
          serializer);
      List<PlainT> plainEs = new ArrayList<>(epsilons.size());
      List<PlainT> plainDs = new ArrayList<>(epsilons.size());
      PlainT zero = resourcePool.getFactory().zero();
      for (int i = 0; i < epsilonAndDelta.size(); i++) {
        // compute [prod] = [c] + epsilons * [b] + deltas * [a] + epsilons * deltas
        PlainT e = epsilonAndDelta.get(i).getFirst();
        PlainT d = epsilonAndDelta.get(i).getSecond();
        PlainT ed = e.multiply(d);
        SInt product = triples.get(i).getProduct()
            .add(triples.get(i).getRight().multiply(e))
            .add(triples.get(i).getLeft().multiply(d))
            .addConstant(ed, macKeyShare, zero, resourcePool.getMyId() == 1);
        plainEs.add(e);
        plainDs.add(d);
        products.add(product);
      }
      resourcePool.getOpenedValueStore().pushOpenedValues(epsilons, plainEs);
      resourcePool.getOpenedValueStore().pushOpenedValues(deltas, plainDs);
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private List<Pair<PlainT, PlainT>> receiveAndReconstruct(Network network, int noOfParties,
      ByteSerializer<PlainT> serializer) {
    List<Pair<PlainT, PlainT>> pairs = new ArrayList<>(epsilons.size());
    long receiveTime = 0L;
    for (int p = 0; p < epsilons.size(); p++) {
      List<PlainT> epsilonShares = new ArrayList<>(noOfParties);
      List<PlainT> deltaShares = new ArrayList<>(noOfParties);
      for (int i = 1; i <= noOfParties; i++) {
        long start = System.currentTimeMillis();
        byte[] rawEpsilon = network.receive(i);
        byte[] rawDelta = network.receive(i);
        long end = System.currentTimeMillis();
        receiveTime += end - start;
        epsilonShares.add(serializer.deserialize(rawEpsilon));
        deltaShares.add(serializer.deserialize(rawDelta));
      }
      PlainT e = UInt.sum(epsilonShares);
      PlainT d = UInt.sum(deltaShares);
      pairs.add(new Pair<>(e, d));
    }
//    System.out.println("Receive," + receiveTime);
    return pairs;
  }

  @Override
  public List<DRes<SInt>> out() {
    return products;
  }

}
