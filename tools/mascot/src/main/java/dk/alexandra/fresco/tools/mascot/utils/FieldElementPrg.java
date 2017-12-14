package dk.alexandra.fresco.tools.mascot.utils;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public interface FieldElementPrg {

  /**
   * Deterministically generates random field element.
   * 
   * @param modulus field modulus
   * @param bitLength bit length of modulus
   * @return random field element
   */
  FieldElement getNext(BigInteger modulus, int bitLength);

  /**
   * Generates multiple random field elements.
   *
   * @param modulus field modulus
   * @param bitLength bit length of modulus
   * @param numSamples number of random elements to generate
   * @return random field element
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
   * @param modulus field modulus
   * @param bitLength bit length of modulus
   * @param numRows number of rows of random elements
   * @param numCols number of values per row
   * @return random field element
   */
  default List<List<FieldElement>> getNext(BigInteger modulus, int bitLength, int numRows,
      int numCols) {
    List<List<FieldElement>> sampled = new ArrayList<>(numRows);
    for (int i = 0; i < numRows; i++) {
      sampled.add(getNext(modulus, bitLength, numCols));
    }
    return sampled;
  }

}
