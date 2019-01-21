package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpdzAndBatchedProtocol extends SpdzNativeProtocol<List<DRes<SInt>>> {

  private final DRes<List<DRes<SInt>>> leftDef;
  private final DRes<List<DRes<SInt>>> rightDef;
  private List<SpdzTriple> triples;
  private List<SpdzSInt> epsilons;
  private List<SpdzSInt> deltas;
  private List<BigInteger> openEpsilons;
  private List<BigInteger> openDeltas;
  private List<DRes<SInt>> products;

  public SpdzAndBatchedProtocol(DRes<List<DRes<SInt>>> left, DRes<List<DRes<SInt>>> right) {
    this.leftDef = left;
    this.rightDef = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    SpdzDataSupplier dataSupplier = spdzResourcePool.getDataSupplier();
    int noOfPlayers = spdzResourcePool.getNoOfParties();
    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();
    final List<DRes<SInt>> leftFactors = leftDef.out();
    final List<DRes<SInt>> rightFactors = rightDef.out();
    if (leftFactors.size() != rightFactors.size()) {
      throw new IllegalArgumentException("Lists must be same size");
    }
    if (round == 0) {
      triples = new ArrayList<>(leftFactors.size());
      epsilons = new ArrayList<>(leftFactors.size());
      deltas = new ArrayList<>(leftFactors.size());
      openEpsilons = new ArrayList<>(leftFactors.size());
      openDeltas = new ArrayList<>(leftFactors.size());
      products = new ArrayList<>(leftFactors.size());

      for (int i = 0; i < leftFactors.size(); i++) {
        SpdzTriple triple = dataSupplier.getNextTriple();
        triples.add(triple);

        SpdzSInt left = (SpdzSInt) leftFactors.get(i).out();
        SpdzSInt right = (SpdzSInt) rightFactors.get(i).out();

        SpdzSInt epsilon = left.subtract(triple.getA());
        SpdzSInt delta = right.subtract(triple.getB());
        epsilons.add(epsilon);
        deltas.add(delta);
      }

      serializeAndSend(network, serializer);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      BigInteger modulus = spdzResourcePool.getModulus();
      receiveAndReconstruct(network, serializer, noOfPlayers, modulus);
      for (int i = 0; i < leftFactors.size(); i++) {
        BigInteger e = openEpsilons.get(i);
        BigInteger d = openDeltas.get(i);

        SpdzSInt product = computeProduct(spdzResourcePool.getMyId(),
            e, d, modulus, triples.get(i), dataSupplier.getSecretSharedKey());
        products.add(product);
        // Set the opened and closed value.
        spdzResourcePool.getOpenedValueStore().pushOpenedValue(epsilons.get(i), e);
        spdzResourcePool.getOpenedValueStore().pushOpenedValue(deltas.get(i), d);
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  static SpdzSInt computeProduct(int myId, BigInteger e, BigInteger d, BigInteger modulus,
      SpdzTriple triple, BigInteger key) {
    BigInteger product = e.multiply(d).mod(modulus);
    SpdzSInt ed = new SpdzSInt(
        product,
        key.multiply(product).mod(modulus),
        modulus);
    SpdzSInt res = triple.getC();
    return res.add(triple.getB().multiply(e))
        .add(triple.getA().multiply(d))
        .add(ed, myId);
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
  public List<DRes<SInt>> out() {
    return products;
  }

}
