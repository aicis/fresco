package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;

public class TripleValidator {

  // TODO what is the proper way to do this?

  /**
   * Checks that left * right = product.
   * 
   * @param triple triple to check
   * @return true if product false otherwise
   */
  public boolean tripleIsValidProduct(MultTriple triple) {
    AuthenticatedElement left = triple.getLeft();
    AuthenticatedElement right = triple.getRight();
    AuthenticatedElement product = triple.getProduct();

    FieldElement leftValue = left.getShare();
    FieldElement rightValue = right.getShare();
    FieldElement productValue = product.getShare();
    return leftValue.multiply(rightValue)
        .equals(productValue);
  }

  /**
   * Checks that leftMac * rightMac = productMac * macKey.
   * 
   * @param triple triple to check
   * @return true if product false otherwise
   */
  public boolean tripleMacIsValid(MultTriple triple, FieldElement macKey) {
    AuthenticatedElement left = triple.getLeft();
    AuthenticatedElement right = triple.getRight();
    AuthenticatedElement product = triple.getProduct();

    FieldElement leftMac = left.getMac();
    FieldElement rightMac = right.getMac();
    FieldElement productMac = product.getMac();
    return leftMac.multiply(rightMac)
        .equals(productMac.multiply(macKey));
  }

}
