package dk.alexandra.fresco.tools.bitTriples.elements;

import dk.alexandra.fresco.framework.builder.numeric.Addable;

/**
 * A secret-shared authenticated multiplication triple. <p>Holds three elements [a],[b],[c] such
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
    return "MultiplicationTriple [left=" + left.toString() + ", right=" + right.toString() + ", product=" + product.toString() + "]";
  }

  @Override
  public MultiplicationTriple add(MultiplicationTriple other) {
    AuthenticatedElement leftSum = left.xor(other.left);
    AuthenticatedElement rightSum = right.xor(other.right);
    AuthenticatedElement productSum = product.xor(other.product);
    return new MultiplicationTriple(leftSum, rightSum, productSum);
  }

}
