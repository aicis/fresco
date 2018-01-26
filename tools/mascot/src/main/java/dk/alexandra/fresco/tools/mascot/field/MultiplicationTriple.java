package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.tools.mascot.arithm.Addable;

/**
 * An secret-shared authenticated multiplication triple. <p>Holds three elements [a],[b],[c] such
 * that [a * b] = [c]</p>
 */
public class MultiplicationTriple implements Addable<MultiplicationTriple> {

  private final AuthenticatedElement left;
  private final AuthenticatedElement right;
  private final AuthenticatedElement product;

  /**
   * Creates new multiplication triple.
   *
   * @param left left factor
   * @param right right factor
   * @param product product
   */
  public MultiplicationTriple(AuthenticatedElement left, AuthenticatedElement right,
      AuthenticatedElement product) {
    super();
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public AuthenticatedElement getLeft() {
    return left;
  }

  public AuthenticatedElement getRight() {
    return right;
  }

  public AuthenticatedElement getProduct() {
    return product;
  }

  @Override
  public String toString() {
    return "MultiplicationTriple [left=" + left + ", right=" + right + ", product=" + product + "]";
  }

  @Override
  public MultiplicationTriple add(MultiplicationTriple other) {
    AuthenticatedElement leftSum = left.add(other.left);
    AuthenticatedElement rightSum = right.add(other.right);
    AuthenticatedElement productSum = product.add(other.product);
    return new MultiplicationTriple(leftSum, rightSum, productSum);
  }

}
