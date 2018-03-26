package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Spdz2kBatchMultiplier<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<List<DRes<SInt>>, PlainT> {

  private Deque<DRes<SInt>> leftFactors;
  private Deque<DRes<SInt>> rightFactors;
  private Deque<DeferredSInt> deferredProducts;
  private List<Spdz2kTriple<PlainT>> triples;
  private List<Spdz2kSInt<PlainT>> epsilons;
  private List<Spdz2kSInt<PlainT>> deltas;

  public Spdz2kBatchMultiplier() {
    leftFactors = new LinkedList<>();
    rightFactors = new LinkedList<>();
    deferredProducts = new LinkedList<>();
  }

  public DRes<SInt> append(DRes<SInt> left, DRes<SInt> right) {
    DeferredSInt deferred = new DeferredSInt();
    leftFactors.add(left);
    rightFactors.add(right);
    deferredProducts.add(deferred);
    return deferred;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    if (round == 0) {
      this.epsilons = new ArrayList<>(leftFactors.size());
      this.deltas = new ArrayList<>(rightFactors.size());
      triples = resourcePool.getDataSupplier().getNextTripleShares(leftFactors.size());
      int i = 0;
      while (!leftFactors.isEmpty()) {
        Spdz2kSInt<PlainT> left = toSpdz2kSInt(leftFactors.pop());
        Spdz2kSInt<PlainT> epsilon = left.subtract(triples.get(i).getLeft());
        epsilons.add(epsilon);
        Spdz2kSInt<PlainT> right = toSpdz2kSInt(rightFactors.pop());
        Spdz2kSInt<PlainT> delta = right.subtract(triples.get(i).getRight());
        deltas.add(delta);
        i++;
      }
      serializeAndSend(network, resourcePool.getFactory(), epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      List<Pair<PlainT, PlainT>> epsilonAndDelta = receiveAndReconstruct(network,
          resourcePool.getFactory(),
          resourcePool.getNoOfParties()
      );
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
        deferredProducts.pop().callback(product);
      }
      resourcePool.getOpenedValueStore().pushOpenedValues(epsilons, plainEs);
      resourcePool.getOpenedValueStore().pushOpenedValues(deltas, plainDs);
      epsilons.clear();
      deltas.clear();
      return EvaluationStatus.IS_DONE;
    }
  }

  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private List<Pair<PlainT, PlainT>> receiveAndReconstruct(Network network,
      CompUIntFactory<PlainT> factory, int noOfParties) {
    List<Pair<PlainT, PlainT>> pairs = new ArrayList<>(epsilons.size());
    List<PlainT> epsilonShares = new ArrayList<>(epsilons.size());
    List<PlainT> deltaShares = new ArrayList<>(epsilons.size());
    for (int i = 0; i < epsilons.size(); i++) {
      epsilonShares.add(factory.zero());
      deltaShares.add(factory.zero());
    }
    int byteLength = factory.getLowBitLength() / Byte.SIZE;
    for (int i = 1; i <= noOfParties; i++) {
      byte[] rawEpsilons = network.receive(i);
      byte[] rawDeltas = network.receive(i);
      for (int j = 0; j < epsilons.size(); j++) {
        int chunkIndex = j * byteLength;
        epsilonShares.set(j, epsilonShares.get(j)
            .add(factory.createFromBytes(rawEpsilons, chunkIndex, byteLength)));
        deltaShares.set(j, deltaShares.get(j)
            .add(factory.createFromBytes(rawDeltas, chunkIndex, byteLength)));
      }
    }
    for (int j = 0; j < epsilons.size(); j++) {
      pairs.add(new Pair<>(epsilonShares.get(j), deltaShares.get(j)));
    }
    return pairs;
  }

  private void serializeAndSend(Network network, CompUIntFactory<PlainT> factory,
      List<Spdz2kSInt<PlainT>> epsilons, List<Spdz2kSInt<PlainT>> deltas) {
    int byteLength = factory.getLowBitLength() / Byte.SIZE;
    byte[] epsilonBytes = new byte[epsilons.size() * byteLength];
    byte[] deltaBytes = new byte[epsilons.size() * byteLength];
    for (int i = 0; i < epsilons.size(); i++) {
      byte[] serializedEpsilon = epsilons.get(i).getShare().getLeastSignificant().toByteArray();
      byte[] serializedDelta = deltas.get(i).getShare().getLeastSignificant().toByteArray();
      System.arraycopy(serializedEpsilon, 0, epsilonBytes, i * byteLength, byteLength);
      System.arraycopy(serializedDelta, 0, deltaBytes, i * byteLength, byteLength);
    }
    network.sendToAll(epsilonBytes);
    network.sendToAll(deltaBytes);
  }

  @Override
  public List<DRes<SInt>> out() {
    return null;
  }

  private class DeferredSInt implements DRes<SInt> {
    // should this just be a future?

    private SInt value;

    @Override
    public void callback(SInt value) {
      if (this.value != null) {
        throw new IllegalArgumentException("Value already assigned");
      }
      this.value = value;
    }

    @Override
    public SInt out() {
      return value;
    }
  }

}
