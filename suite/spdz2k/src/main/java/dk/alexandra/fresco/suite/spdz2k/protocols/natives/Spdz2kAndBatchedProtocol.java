package dk.alexandra.fresco.suite.spdz2k.protocols.natives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kTriple;
import dk.alexandra.fresco.suite.spdz2k.resource.Spdz2kResourcePool;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Native protocol for computing logical AND of two values in boolean form.
 */
public class Spdz2kAndBatchedProtocol<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kNativeProtocol<List<DRes<SInt>>, PlainT> {

  private DRes<List<DRes<SInt>>> bitsADef;
  private DRes<List<DRes<SInt>>> bitsBDef;
  private List<Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>>> triples;
  private List<Spdz2kSIntBoolean<PlainT>> epsilons;
  private List<Spdz2kSIntBoolean<PlainT>> deltas;
  private List<PlainT> openEpsilons;
  private List<PlainT> openDeltas;
  private List<DRes<SInt>> products;

  public Spdz2kAndBatchedProtocol(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB) {
    this.bitsADef = bitsA;
    this.bitsBDef = bitsB;
  }

  @Override
  public EvaluationStatus evaluate(int round, Spdz2kResourcePool<PlainT> resourcePool,
      Network network) {
    PlainT macKeyShare = resourcePool.getDataSupplier().getSecretSharedKey();
    CompUIntFactory<PlainT> factory = resourcePool.getFactory();
    List<DRes<SInt>> bitsA = bitsADef.out();
    List<DRes<SInt>> bitsB = bitsBDef.out();
    if (round == 0) {
      triples = new ArrayList<>(bitsA.size());
      epsilons = new ArrayList<>(bitsA.size());
      deltas = new ArrayList<>(bitsA.size());
      openEpsilons = new ArrayList<>(bitsA.size());
      openDeltas = new ArrayList<>(bitsA.size());
      products = new ArrayList<>(bitsA.size());

      for (int i = 0; i < bitsA.size(); i++) {
        Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> triple = resourcePool
            .getDataSupplier()
            .getNextBitTripleShares();
        triples.add(triple);

        Spdz2kSIntBoolean<PlainT> left = factory.toSpdz2kSIntBoolean(bitsA.get(i));
        Spdz2kSIntBoolean<PlainT> right = factory.toSpdz2kSIntBoolean(bitsB.get(i));

        epsilons.add(factory.toSpdz2kSIntBoolean(left).xor(triple.getLeft()));
        deltas.add(factory.toSpdz2kSIntBoolean(right).xor(triple.getRight()));
      }

      serializeAndSend(network, epsilons, deltas);
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      receiveAndReconstruct(network, factory, resourcePool.getNoOfParties());

      resourcePool.getOpenedValueStore().pushOpenedValues(
          epsilons.stream().map(Spdz2kSIntBoolean::asArithmetic).collect(Collectors.toList()),
          openEpsilons.stream().map(e -> e.toArithmeticRep()).collect(Collectors.toList())
      );
      resourcePool.getOpenedValueStore().pushOpenedValues(
          deltas.stream().map(Spdz2kSIntBoolean::asArithmetic).collect(Collectors.toList()),
          openDeltas.stream().map(e -> e.toArithmeticRep()).collect(Collectors.toList())
      );

      for (int i = 0; i < bitsA.size(); i++) {
        Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> triple = triples.get(i);

        PlainT e = openEpsilons.get(i);
        PlainT d = openDeltas.get(i);

        Spdz2kSIntBoolean<PlainT> prod = mult(e, d, triple, macKeyShare, factory,
            resourcePool.getMyId());

        products.add(prod);
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  private Spdz2kSIntBoolean<PlainT> mult(
      PlainT e,
      PlainT d,
      Spdz2kTriple<PlainT, Spdz2kSIntBoolean<PlainT>> triple,
      PlainT macKeyShare,
      CompUIntFactory<PlainT> factory,
      int myId) {
    int eBit = e.bitValue();
    int dBit = d.bitValue();
    PlainT ed = e.multiply(d);
    // compute [prod] = [c] XOR epsilon AND [b] XOR delta AND [a] XOR epsilon AND delta
    Spdz2kSIntBoolean<PlainT> tripleLeft = triple.getLeft();
    Spdz2kSIntBoolean<PlainT> tripleRight = triple.getRight();
    Spdz2kSIntBoolean<PlainT> tripleProduct = triple.getProduct();
    return tripleProduct
        .xor(tripleRight.and(eBit))
        .xor(tripleLeft.and(dBit))
        .xorOpen(ed,
            macKeyShare,
            factory.zero().toBitRep(),
            myId == 1);
  }

  /**
   * Retrieves shares for epsilons and deltas and reconstructs each.
   */
  private void receiveAndReconstruct(Network network, CompUIntFactory<PlainT> factory,
      int noOfParties) {
    byte[] rawEpsilons = network.receive(1);
    byte[] rawDeltas = network.receive(1);
    for (int i = 0; i < epsilons.size(); i++) {
      openEpsilons.add(factory.fromBit(rawEpsilons[i]));
      openDeltas.add(factory.fromBit(rawDeltas[i]));
    }
    for (int i = 2; i <= noOfParties; i++) {
      rawEpsilons = network.receive(i);
      rawDeltas = network.receive(i);

      for (int j = 0; j < epsilons.size(); j++) {
        openEpsilons.set(j, openEpsilons.get(j).add(factory.fromBit(rawEpsilons[j])));
        openDeltas.set(j, openDeltas.get(j).add(factory.fromBit(rawDeltas[j])));
      }
    }
  }

  /**
   * Serializes and sends epsilon and delta values.
   */
  private void serializeAndSend(Network network,
      List<Spdz2kSIntBoolean<PlainT>> epsilons,
      List<Spdz2kSIntBoolean<PlainT>> deltas) {
    byte[] epsilonBytes = new byte[epsilons.size()];
    byte[] deltaBytes = new byte[epsilons.size()];
    for (int i = 0; i < epsilons.size(); i++) {
      byte[] serializedEpsilon = epsilons.get(i).serializeShareLow();
      epsilonBytes[i] = serializedEpsilon[0];
      byte[] serializedDelta = deltas.get(i).serializeShareLow();
      deltaBytes[i] = serializedDelta[0];
    }
    network.sendToAll(epsilonBytes);
    network.sendToAll(deltaBytes);
  }

  @Override
  public List<DRes<SInt>> out() {
    return products;
  }

}
