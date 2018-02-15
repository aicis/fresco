package dk.alexandra.fresco.decimal;

public interface RealNumeric {

  /**
   * Get basic arithmetic functionality
   * 
   * @return
   */
  public BasicRealNumeric numeric();

  public DefaultAdvancedRealNumeric advanced();

  public DefaultLinearAlgebra linalg();

}
