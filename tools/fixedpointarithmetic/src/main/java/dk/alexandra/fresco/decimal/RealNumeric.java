package dk.alexandra.fresco.decimal;

public interface RealNumeric {

  /**
   * Get basic arithmetic functionality
   * 
   * @return
   */
  public BasicRealNumeric numeric();

  public AdvancedRealNumeric advanced();

  public LinearAlgebra linalg();

}
