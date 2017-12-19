package dk.alexandra.fresco.tools.mascot.field;

import dk.alexandra.fresco.tools.mascot.arithm.Addable;

public class MultTriple implements Addable<MultTriple> {

  private AuthenticatedElement left;
  private AuthenticatedElement right;
  private AuthenticatedElement product;

  /**
   * Creates new multiplication triple.
   * 
   * @param left left factor
   * @param right right factor
   * @param product product
   */
  public MultTriple(AuthenticatedElement left, AuthenticatedElement right,
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
    return "MultTriple [left=" + left + ", right=" + right + ", product=" + product + "]";
  }

  @Override
  public MultTriple add(MultTriple other) {
    AuthenticatedElement leftSum = left.add(other.left);
    AuthenticatedElement rightSum = right.add(other.right);
    AuthenticatedElement productSum = product.add(other.product);
    return new MultTriple(leftSum, rightSum, productSum);
  }

}
