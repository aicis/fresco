package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;

public interface AdvancedFixedNumeric extends ComputationDirectory {

  /**
   * Calculate the exponential function of a secret value.
   * 
   * @param x Input
   * @return
   */
  public DRes<SFixed> exp(DRes<SFixed> x);
    
  /**
   * Create a random value between 0 and 1.
   * 
   * @return The random value 
   */
  public DRes<SFixed> random();
  
}
