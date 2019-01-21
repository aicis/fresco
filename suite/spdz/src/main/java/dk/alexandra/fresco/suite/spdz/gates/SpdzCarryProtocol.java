package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.SIntPair;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpdzCarryProtocol extends SpdzNativeProtocol<List<SIntPair>> {

  private final List<SIntPair> bits;
  private List<SpdzTriple> triples;
  private List<SpdzSInt> epsilons;
  private List<SpdzSInt> deltas;
  private List<BigInteger> openEpsilons;
  private List<BigInteger> openDeltas;
  private List<SIntPair> carried;

  public SpdzCarryProtocol(List<SIntPair> bits) {
    this.bits = bits;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool, Network network) {
    SpdzDataSupplier dataSupplier = spdzResourcePool.getDataSupplier();
    int noOfPlayers = spdzResourcePool.getNoOfParties();
    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      triples = new ArrayList<>(bits.size());
      epsilons = new ArrayList<>(bits.size());
      deltas = new ArrayList<>(bits.size());
      openEpsilons = new ArrayList<>(bits.size());
      openDeltas = new ArrayList<>(bits.size());
      carried = new ArrayList<>(bits.size() / 2);

      for (int i = 0; i < bits.size() / 2; i++) {
        // two multiplications
        triples.add(spdzResourcePool.getDataSupplier().getNextTriple());
        triples.add(spdzResourcePool.getDataSupplier().getNextTriple());

        SIntPair left = bits.get(2 * i + 1);
        SIntPair right = bits.get(2 * i);

        SpdzTriple p1p2Triple = triples.get(2 * i + 1);
        SpdzTriple p2g1Triple = triples.get(2 * i);

        SpdzSInt p1 = (SpdzSInt) left.getFirst().out();
        SpdzSInt g1 = (SpdzSInt) left.getSecond().out();
        SpdzSInt p2 = (SpdzSInt) right.getFirst().out();

        // p2 * g1
        final SpdzSInt epsilonP2G1 = p2.subtract(p2g1Triple.getA());
        epsilons.add(epsilonP2G1);
        final SpdzSInt deltaP2G1 = g1.subtract(p2g1Triple.getB());
        deltas.add(deltaP2G1);

        // p1 * p2
        final SpdzSInt epsilonP1P2 = p1.subtract(p1p2Triple.getA());
        epsilons.add(epsilonP1P2);
        final SpdzSInt deltaP1P2 = p2.subtract(p1p2Triple.getB());
        deltas.add(deltaP1P2);
      }
      serializeAndSend(network, serializer);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      OpenedValueStore<SpdzSInt, BigInteger> openedValueStore = spdzResourcePool
          .getOpenedValueStore();
      receiveAndReconstruct(network, serializer, noOfPlayers, spdzResourcePool.getModulus());
      for (int i = 0; i < bits.size() / 2; i++) {
        SpdzTriple p1p2Triple = triples.get(2 * i + 1);
        SpdzTriple p2g1Triple = triples.get(2 * i);

        BigInteger p1p2E = openEpsilons.get(2 * i + 1);
        openedValueStore.pushOpenedValue(epsilons.get(2 * i + 1), p1p2E);
        BigInteger p2g1E = openEpsilons.get(2 * i);
        openedValueStore.pushOpenedValue(epsilons.get(2 * i), p2g1E);

        BigInteger p1p2D = openDeltas.get(2 * i + 1);
        openedValueStore.pushOpenedValue(deltas.get(2 * i + 1), p1p2D);
        BigInteger p2g1D = openDeltas.get(2 * i);
        openedValueStore.pushOpenedValue(deltas.get(2 * i), p2g1D);

        SpdzSInt p = SpdzAndBatchedProtocol.computeProduct(spdzResourcePool.getMyId(), p1p2E, p1p2D,
            spdzResourcePool.getModulus(), p1p2Triple, dataSupplier.getSecretSharedKey());

        SpdzSInt g2 = (SpdzSInt) bits.get(2 * i).getSecond().out();

        SpdzSInt g = SpdzAndBatchedProtocol.computeProduct(spdzResourcePool.getMyId(), p2g1E, p2g1D,
            spdzResourcePool.getModulus(), p2g1Triple, dataSupplier.getSecretSharedKey())
            .add(g2);
        carried.add(new SIntPair(p, g));
      }
      // if we have an odd number of elements the last pair can just be taken directly from the input
      if (bits.size() % 2 != 0) {
        carried.add(bits.get(bits.size() - 1));
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  private void serializeAndSend(Network network, ByteSerializer<BigInteger> serializer) {
    byte[] epsilonBytes = serializer.serialize(
        epsilons.stream().map(SpdzSInt::getShare).collect(Collectors.toList()));
    byte[] deltaBytes = serializer.serialize(
        deltas.stream().map(SpdzSInt::getShare).collect(Collectors.toList()));
    network.sendToAll(epsilonBytes);
    network.sendToAll(deltaBytes);
  }

  private void receiveAndReconstruct(Network network, ByteSerializer<BigInteger> serializer,
      int noOfParties, BigInteger modulus) {
    byte[] rawEpsilons = network.receive(1);
    byte[] rawDeltas = network.receive(1);
    openEpsilons = serializer.deserializeList(rawEpsilons);
    openDeltas = serializer.deserializeList(rawDeltas);

    for (int i = 2; i <= noOfParties; i++) {
      rawEpsilons = network.receive(i);
      rawDeltas = network.receive(i);

      List<BigInteger> innerEpsilons = serializer.deserializeList(rawEpsilons);
      List<BigInteger> innerDeltas = serializer.deserializeList(rawDeltas);
      for (int j = 0; j < epsilons.size(); j++) {
        openEpsilons.set(j, openEpsilons.get(j).add(innerEpsilons.get(j)));
        openDeltas.set(j, openDeltas.get(j).add(innerDeltas.get(j)));
      }
    }

    for (int j = 0; j < epsilons.size(); j++) {
      openEpsilons.set(j, openEpsilons.get(j).mod(modulus));
      openDeltas.set(j, openDeltas.get(j).mod(modulus));
    }
  }

  @Override
  public List<SIntPair> out() {
    return carried;
  }
}
