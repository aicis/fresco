package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class Sampler {

  Random rand;

  public Sampler(Random rand) {
    this.rand = rand;
  }

  public FieldElement sample(BigInteger modulus, int bitLength) {
    BigInteger raw = new BigInteger(bitLength, rand).mod(modulus);
    return new FieldElement(raw, modulus, bitLength);
  }

  public List<FieldElement> sample(BigInteger modulus, int bitLength, int numSamples) {
    List<FieldElement> samples = new ArrayList<>();
    for (int i = 0; i < numSamples; i++) {
      samples.add(sample(modulus, bitLength));
    }
    return samples;
  }

}
