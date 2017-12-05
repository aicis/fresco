package dk.alexandra.fresco.tools.mascot.utils.sample;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class DummyJointSampler implements Sampler {

  Sampler dummySampler;

  public DummyJointSampler() {
    this.dummySampler = new DummySampler(new Random(1));
  }

  @Override
  public FieldElement sample(BigInteger modulus, int bitLength) {
    return dummySampler.sample(modulus, bitLength);
  }

  @Override
  public List<FieldElement> sample(BigInteger modulus, int bitLength, int numSamples) {
    return dummySampler.sample(modulus, bitLength, numSamples);
  }

  @Override
  public List<List<FieldElement>> sample(BigInteger modulus, int modBitLength, int numGroups,
      int groupSize) {
    return dummySampler.sample(modulus, modBitLength, numGroups, groupSize);
  }

}
