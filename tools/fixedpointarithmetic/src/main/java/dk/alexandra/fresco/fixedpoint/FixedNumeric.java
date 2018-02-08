package dk.alexandra.fresco.fixedpoint;

public interface FixedNumeric {

  /**
   * Get basic arithmetic functionality
   * @return
   */
  public BasicFixedNumeric numeric();

  public AdvancedFixedNumeric advanced();
  
  public LinearAlgebra linalg();
  
}
