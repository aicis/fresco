package dk.alexandra.fresco.tools.mascot.elgen;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.util.List;

public interface Sharer {

  /**
   * Creates secret shares of input field element.
   * 
   * @param input field element to secret-share
   * @param numShares number of shares to generate
   * @return secret shares
   */
  List<FieldElement> share(FieldElement input, int numShares);

  /**
   * Recombines secret shares into input element.
   * 
   * @param shares shares to recombine
   * @return recombines shares
   */
  FieldElement recombine(List<FieldElement> shares);

}
