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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((left == null) ? 0 : left.hashCode());
    result = prime * result + ((product == null) ? 0 : product.hashCode());
    result = prime * result + ((right == null) ? 0 : right.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MultTriple other = (MultTriple) obj;
    if (left == null) {
      if (other.left != null) {
        return false;
      }
    } else if (!left.equals(other.left)) {
      return false;
    }
    if (product == null) {
      if (other.product != null) {
        return false;
      }
    } else if (!product.equals(other.product)) {
      return false;
    }
    if (right == null) {
      if (other.right != null) {
        return false;
      }
    } else if (!right.equals(other.right)) {
      return false;
    }
    return true;
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
