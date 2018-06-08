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

public class SpdzMultProtocol extends SpdzNativeProtocol<SInt> {

  private DRes<SInt> left;
  private DRes<SInt> right;
  private SpdzSInt out;
  private SpdzTriple triple;
  private SpdzSInt epsilon;  // my share of the differences [x]-[a]
  private SpdzSInt delta;  // and [y]-[b].

  public SpdzMultProtocol(DRes<SInt> left, DRes<SInt> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool spdzResourcePool,
      Network network) {
    SpdzDataSupplier dataSupplier = spdzResourcePool.getDataSupplier();
    int noOfPlayers = spdzResourcePool.getNoOfParties();
    ByteSerializer<BigInteger> serializer = spdzResourcePool.getSerializer();
    if (round == 0) {
      this.triple = dataSupplier.getNextTriple();

      epsilon = ((SpdzSInt) left.out()).subtract(triple.getA());
      delta = ((SpdzSInt) right.out()).subtract(triple.getB());

      network.sendToAll(serializer.serialize(epsilon.getShare()));
      network.sendToAll(serializer.serialize(delta.getShare()));
      return EvaluationStatus.HAS_MORE_ROUNDS;
    } else {
      BigInteger[] epsilonShares = new BigInteger[noOfPlayers];
      BigInteger[] deltaShares = new BigInteger[noOfPlayers];
      for (int i = 0; i < noOfPlayers; i++) {
        epsilonShares[i] = serializer.deserialize(network.receive(i + 1));
        deltaShares[i] = serializer.deserialize(network.receive(i + 1));
      }

      BigInteger e = epsilonShares[0];
      BigInteger d = deltaShares[0];
      for (int i = 1; i < epsilonShares.length; i++) {
        e = e.add(epsilonShares[i]);
        d = d.add(deltaShares[i]);
      }
      BigInteger modulus = spdzResourcePool.getModulus();
      e = e.mod(modulus);
      d = d.mod(modulus);

      BigInteger product = e.multiply(d).mod(modulus);
      SpdzSInt ed = new SpdzSInt(
          product,
          dataSupplier.getSecretSharedKey().multiply(product).mod(modulus),
          modulus);
      SpdzSInt res = triple.getC();
      out = res.add(triple.getB().multiply(e))
          .add(triple.getA().multiply(d))
          .add(ed, spdzResourcePool.getMyId());
      // Set the opened and closed value.
      spdzResourcePool.getOpenedValueStore().pushOpenedValue(epsilon, e);
      spdzResourcePool.getOpenedValueStore().pushOpenedValue(delta, d);
      return EvaluationStatus.IS_DONE;
    }
  }

  @Override
  public SpdzSInt out() {
    return out;
  }

}
