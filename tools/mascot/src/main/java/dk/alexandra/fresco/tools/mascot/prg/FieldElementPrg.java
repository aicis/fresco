package dk.alexandra.fresco.tools.mascot.prg;

import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import java.util.ArrayList;
import java.util.List;

public interface FieldElementPrg {

  /**
   * Deterministically generates random field element.
   *
   * @return random field element
   */
  FieldElement getNext();

  /**
   * Generates multiple random field elements.
   *
   * @param numSamples number of random elements to generate
   * @return random field element
   */
  default List<FieldElement> getNext(int numSamples) {
    List<FieldElement> samples = new ArrayList<>();
    for (int i = 0; i < numSamples; i++) {
      samples.add(getNext());
    }
    return samples;
  }

  /**
   * Generates a matrix of random field elements.
   *
   * @param numRows number of rows of random elements
   * @param numCols number of values per row
   * @return random field element
   */
  default List<List<FieldElement>> getNext(int numRows,
      int numCols) {
    List<List<FieldElement>> sampled = new ArrayList<>(numRows);
    for (int i = 0; i < numRows; i++) {
      sampled.add(getNext(numCols));
    }
    return sampled;
  }
}
