package dk.alexandra.fresco.tools.mascot.utils.sample;

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public interface Sampler {

  public FieldElement sample(BigInteger modulus, int bitLength);

  public List<FieldElement> sample(BigInteger modulus, int bitLength, int numSamples);

  public List<List<FieldElement>> sample(BigInteger modulus, int modBitLength, int numGroups,
      int groupSize);  
}
