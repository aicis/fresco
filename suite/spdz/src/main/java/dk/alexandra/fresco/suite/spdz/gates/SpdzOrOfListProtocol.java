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

public class SpdzOrOfListProtocol extends SpdzNativeProtocol<SInt> {

  private final DRes<List<DRes<SInt>>> bitsDef;
  private SInt res;
  private List<DRes<SInt>> nextRound;
  private List<DRes<SInt>> bitsA;
  private List<DRes<SInt>> bitsB;
  private List<SpdzTriple> triples;
  private List<SpdzSInt> epsilons;
  private List<SpdzSInt> deltas;
  private List<BigInteger> openEpsilons;
  private List<BigInteger> openDeltas;
  private SInt extraBit;

  public SpdzOrOfListProtocol(DRes<List<DRes<SInt>>> left) {
    this.bitsDef = left;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    SpdzDataSupplier dataSupplier = spdzResourcePool.getDataSupplier();
    int noOfPlayers = spdzResourcePool.getNoOfParties();
    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();

//    System.out.println("Running round " + round);
    if (round % 2 == 0) {
      if (nextRound == null) {
        nextRound = bitsDef.out();
      }
      final int nextRoundSize = nextRound.size();
      if (nextRoundSize == 1) {
        res = nextRound.get(0).out();
        return EvaluationStatus.IS_DONE;
      }

      bitsA = new ArrayList<>(nextRoundSize / 2);
      bitsB = new ArrayList<>(nextRoundSize / 2);
      for (int i = 0; i < nextRoundSize - 1; i += 2) {
        bitsA.add(nextRound.get(i));
        bitsB.add(nextRound.get(i + 1));
      }
      triples = new ArrayList<>(nextRoundSize / 2);
      epsilons = new ArrayList<>(nextRoundSize / 2);
      deltas = new ArrayList<>(nextRoundSize / 2);
      openEpsilons = new ArrayList<>(nextRoundSize / 2);
      openDeltas = new ArrayList<>(nextRoundSize / 2);
      // reset next round
      final boolean isOdd = nextRoundSize % 2 != 0;
      if (isOdd) {
        extraBit = nextRound.get(nextRoundSize - 1).out();
      } else {
        extraBit = null;
      }
      nextRound = new ArrayList<>(nextRoundSize / 2);
      for (int i = 0; i < bitsA.size(); i++) {
        SpdzTriple triple = dataSupplier.getNextTriple();
        triples.add(triple);

        SpdzSInt left = (SpdzSInt) bitsA.get(i).out();
        SpdzSInt right = (SpdzSInt) bitsB.get(i).out();

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
      for (int i = 0; i < bitsA.size(); i++) {
        BigInteger e = openEpsilons.get(i);
        BigInteger d = openDeltas.get(i);

        SpdzSInt product = SpdzAndBatchedProtocol.computeProduct(spdzResourcePool.getMyId(),
            e, d, modulus, triples.get(i), dataSupplier.getSecretSharedKey());

        SpdzSInt left = (SpdzSInt) bitsA.get(i).out();
        SpdzSInt right = (SpdzSInt) bitsB.get(i).out();
        // bitA + bitB - bitA * bitB
        SpdzSInt disjunction = left.add(right).subtract(product);
        nextRound.add(disjunction);
        // Set the opened and closed value.
        spdzResourcePool.getOpenedValueStore().pushOpenedValue(epsilons.get(i), e);
        spdzResourcePool.getOpenedValueStore().pushOpenedValue(deltas.get(i), d);
      }
      if (extraBit != null) {
        nextRound.add(extraBit);
      }
      return EvaluationStatus.HAS_MORE_ROUNDS;
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
  public SInt out() {
    return res;
  }

}
