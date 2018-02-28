package dk.alexandra.fresco.decimal;

public interface RealNumeric {

  /**
   * Get basic arithmetic functionality for SReals's.
   *
   * @return
   */
  public BasicRealNumeric numeric();

  /**
   * Get advanced mathematical functions for SReals's.
   *
   * @return
   */
  public AdvancedRealNumeric advanced();

  /**
   * Get linear algebra functionality for SReals's.
   *
   * @return
   */
  public LinearAlgebra linalg();

}
