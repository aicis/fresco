package dk.alexandra.fresco.tools.mascot.prg;

import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public interface FieldElementPrg {

  /**
   * Deterministically generates random field element.
   * 
   * @param modulus field modulus
   * @return random field element
   */
  MascotFieldElement getNext(Modulus modulus);

  /**
   * Generates multiple random field elements.
   *
   * @param modulus field modulus
   * @param numSamples number of random elements to generate
   * @return random field element
   */
  default List<MascotFieldElement> getNext(Modulus modulus, int numSamples) {
    List<MascotFieldElement> samples = new ArrayList<>();
    for (int i = 0; i < numSamples; i++) {
      samples.add(getNext(modulus));
    }
    return samples;
  }

  /**
   * Generates a matrix of random field elements.
   * 
   * @param modulus field modulus
   * @param numRows number of rows of random elements
   * @param numCols number of values per row
   * @return random field element
   */
  default List<List<MascotFieldElement>> getNext(Modulus modulus, int numRows,
      int numCols) {
    List<List<MascotFieldElement>> sampled = new ArrayList<>(numRows);
    for (int i = 0; i < numRows; i++) {
      sampled.add(getNext(modulus, numCols));
    }
    return sampled;
  }

}
