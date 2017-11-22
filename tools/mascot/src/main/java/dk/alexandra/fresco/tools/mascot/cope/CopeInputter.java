package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.net.ExtendedNetwork;

public class CopeInputter extends CopeShared {

  private List<Pair<BigInteger, BigInteger>> seeds;

  public CopeInputter(Integer myId, Integer otherId, int kBitLength, int lambdaSecurityParam,
      Random rand, ExtendedNetwork network, ExecutorService executor, BigInteger modulus) {
    super(myId, otherId, kBitLength, lambdaSecurityParam, rand, network, executor, modulus);
    this.seeds = new ArrayList<>();
  }

  public void initialize() {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // TODO: this could just be a ROT
    for (int i = 0; i < kBitLength; i++) {
      BigInteger seedZero = new BigInteger(lambdaSecurityParam, rand);
      BigInteger seedFirst = new BigInteger(lambdaSecurityParam, rand);
      seeds.add(new Pair<>(seedZero, seedFirst));
    }
    ot.send(seeds);
    System.out.println(seeds);
    initialized = true;
  }

  public FieldElement extend(FieldElement inputElement) {
    if (!initialized) {
      throw new IllegalStateException("Cannot call extend before initializing");
    }

    List<Pair<FieldElement, FieldElement>> tValues = seeds.parallelStream().map((seedPair) -> {
      // TODO: make sure real implementation of prf is thread-safe for parallel calls
      FieldElement t0 = this.prf.evaluate(seedPair.getFirst(), counter, modulus, kBitLength);
      FieldElement t1 = this.prf.evaluate(seedPair.getSecond(), counter, modulus, kBitLength);
      return new Pair<>(t0, t1);
    }).collect(Collectors.toList());

    List<FieldElement> uValues = tValues.parallelStream().map((tPair) -> {
      return tPair.getFirst().subtract(tPair.getSecond()).add(inputElement);
    }).collect(Collectors.toList());

    for (FieldElement uValue : uValues) {
      network.send(otherId, uValue.toByteArray());
    }

    List<FieldElement> tZeroValues =
        tValues.parallelStream().map(seedPair -> seedPair.getFirst()).collect(Collectors.toList());
    FieldElement productShare = FieldElement.recombine(tZeroValues, modulus, kBitLength);

    counter = counter.add(BigInteger.ONE);
    return productShare.negate();
  }

  public List<FieldElement> extend(List<FieldElement> inputElements) {
    List<FieldElement> shares = new ArrayList<>();
    for (FieldElement inputElement : inputElements) {
      shares.add(extend(inputElement));
    }
    return shares;
  }

}