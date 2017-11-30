package dk.alexandra.fresco.tools.mascot.utils.sample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class DummySampler implements Sampler {
  Random rand;
  Random jointRandom;

  public DummySampler(Random rand) {
    this.rand = rand;
    this.jointRandom = new Random(1);
  }

  private FieldElement sample(BigInteger modulus, int bitLength, Random rnd) {
    BigInteger raw = new BigInteger(bitLength, rnd).mod(modulus);
    return new FieldElement(raw, modulus, bitLength);
  }

  @Override
  public FieldElement sample(BigInteger modulus, int bitLength) {
    return sample(modulus, bitLength, this.rand);
  }

  @Override
  public List<FieldElement> sample(BigInteger modulus, int bitLength, int numSamples) {
    List<FieldElement> samples = new ArrayList<>();
    for (int i = 0; i < numSamples; i++) {
      samples.add(sample(modulus, bitLength));
    }
    return samples;
  }

  @Override
  public FieldElement jointSample(BigInteger modulus, int bitLength) {
    return sample(modulus, bitLength, jointRandom);
  }

  @Override
  public List<FieldElement> jointSample(BigInteger modulus, int bitLength, int numSamples) {
    List<FieldElement> samples = new ArrayList<>();
    for (int i = 0; i < numSamples; i++) {
      samples.add(jointSample(modulus, bitLength));
    }
    return samples;
  }

  @Override
  public List<List<FieldElement>> sampleGroups(BigInteger modulus, int modBitLength, int numGroups,
      int groupSize) {
    return IntStream.range(0, numGroups)
        .mapToObj(idx -> {
          return sample(modulus, modBitLength, groupSize);
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<List<FieldElement>> jointSampleGroups(BigInteger modulus, int modBitLength,
      int numGroups, int groupSize) {
    return IntStream.range(0, numGroups)
        .mapToObj(idx -> {
          return jointSample(modulus, modBitLength, groupSize);
        })
        .collect(Collectors.toList());
  }

}
