package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public interface FieldElementPrg {

  /**
   * Deterministically generates random field element.
   * 
   * @param modulus
   * @param bitLength
   * @return
   */
  FieldElement getNext(BigInteger modulus, int bitLength);

  /**
   * Generates multiple random field elements.
   * 
   * @param modulus
   * @param bitLength
   * @param numSamples
   * @return
   */
  default List<FieldElement> getNext(BigInteger modulus, int bitLength, int numSamples) {
    List<FieldElement> samples = new ArrayList<>();
    for (int i = 0; i < numSamples; i++) {
      samples.add(getNext(modulus, bitLength));
    }
    return samples;
  }

  /**
   * Generates a matrix of random field elements.
   * 
   * @param modulus
   * @param bitLength
   * @param numSamples
   * @return
   */
  default List<List<FieldElement>> getNext(BigInteger modulus, int modBitLength, int numGroups,
      int groupSize) {
    List<List<FieldElement>> sampled = new ArrayList<>(numGroups);
    for (int i = 0; i < numGroups; i++) {
      sampled.add(getNext(modulus, modBitLength, groupSize));
    }
    return sampled;
  }

}
