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

public class SpdzAndBatchedProtocol extends SpdzNativeProtocol<List<DRes<SInt>>> {

  private final DRes<List<DRes<SInt>>> leftDef;
  private final DRes<List<DRes<SInt>>> rightDef;
  private List<SpdzTriple> triples;
  private List<SpdzSInt> epsilons;
  private List<SpdzSInt> deltas;
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

        network.sendToAll(serializer.serialize(epsilon.getShare()));
        network.sendToAll(serializer.serialize(delta.getShare()));
      }

      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      for (int i = 0; i < leftFactors.size(); i++) {
        BigInteger[] epsilonShares = new BigInteger[noOfPlayers];
        BigInteger[] deltaShares = new BigInteger[noOfPlayers];
        for (int p = 0; p < noOfPlayers; p++) {
          epsilonShares[p] = serializer.deserialize(network.receive(p + 1));
          deltaShares[p] = serializer.deserialize(network.receive(p + 1));
        }

        BigInteger e = epsilonShares[0];
        BigInteger d = deltaShares[0];
        for (int p = 1; p < epsilonShares.length; p++) {
          e = e.add(epsilonShares[p]);
          d = d.add(deltaShares[p]);
        }
        BigInteger modulus = spdzResourcePool.getModulus();
        e = e.mod(modulus);
        d = d.mod(modulus);

        BigInteger product = e.multiply(d).mod(modulus);
        SpdzSInt ed = new SpdzSInt(
            product,
            dataSupplier.getSecretSharedKey().multiply(product).mod(modulus),
            modulus);
        SpdzTriple triple = triples.get(i);
        SpdzSInt res = triple.getC();
        SpdzSInt prod = res.add(triple.getB().multiply(e))
            .add(triple.getA().multiply(d))
            .add(ed, spdzResourcePool.getMyId());
        products.add(prod);
        // Set the opened and closed value.
        spdzResourcePool.getOpenedValueStore().pushOpenedValue(epsilons.get(i), e);
        spdzResourcePool.getOpenedValueStore().pushOpenedValue(deltas.get(i), d);
      }
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public List<DRes<SInt>> out() {
    return products;
  }

}
