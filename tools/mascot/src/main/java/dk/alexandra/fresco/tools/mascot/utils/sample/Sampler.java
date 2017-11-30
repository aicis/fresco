package dk.alexandra.fresco.tools.mascot.utils.sample;

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

// TODO have a sample base class with concrete implementations of local sampler and joint sampler
public interface Sampler {

  public FieldElement sample(BigInteger modulus, int bitLength);

  public List<FieldElement> sample(BigInteger modulus, int bitLength, int numSamples);

  public FieldElement jointSample(BigInteger modulus, int bitLength);

  public List<FieldElement> jointSample(BigInteger modulus, int bitLength, int numSamples);

  public List<List<FieldElement>> sampleGroups(BigInteger modulus, int modBitLength, int numGroups,
      int groupSize);

  public List<List<FieldElement>> jointSampleGroups(BigInteger modulus, int modBitLength,
      int numGroups, int groupSize);

}
