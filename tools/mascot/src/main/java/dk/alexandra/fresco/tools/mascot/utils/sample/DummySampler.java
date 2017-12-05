package dk.alexandra.fresco.tools.mascot.utils.sample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class DummySampler implements Sampler {
  Random rand;

  public DummySampler(Random rand) {
    this.rand = rand;
  }

  @Override
  public FieldElement sample(BigInteger modulus, int bitLength) {
    BigInteger raw = new BigInteger(bitLength, rand).mod(modulus);
    return new FieldElement(raw, modulus, bitLength);
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
  public List<List<FieldElement>> sample(BigInteger modulus, int modBitLength, int numGroups,
      int groupSize) {
    List<List<FieldElement>> sampled = new ArrayList<>(numGroups);
    for (int i = 0; i < numGroups; i++) {
      sampled.add(sample(modulus, modBitLength, groupSize));
    }
    return sampled;
  }

}
